package uk.sky.etl.load

import cats.effect.{Resource, Sync}
import cats.syntax.all._
import uk.sky.etl.models.TransformedPerson
import java.sql.{Connection, DriverManager, PreparedStatement}

/**
 * Trait for loading transformed data to SQLite database.
 */
trait SQLiteLoader[F[_]] {
  def load(data: List[TransformedPerson], dbPath: String): F[Unit]
}

/**
 * Implementation of SQLiteLoader using JDBC.
 */
class SQLiteLoaderImpl[F[_]: Sync] extends SQLiteLoader[F] {

  override def load(
      data: List[TransformedPerson],
      dbPath: String
  ): F[Unit] = {
    val connectionResource = Resource.make(
      Sync[F].delay(DriverManager.getConnection(s"jdbc:sqlite:$dbPath"))
    )(conn => Sync[F].delay(conn.close()))

    connectionResource.use { conn =>
      for {
        _ <- createTable(conn)
        _ <- insertData(conn, data)
      } yield ()
    }
  }

  private def createTable(conn: Connection): F[Unit] = {
    Sync[F].delay {
      val stmt = conn.createStatement()
      try {
        stmt.execute("""
          CREATE TABLE IF NOT EXISTS people (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            full_name TEXT NOT NULL,
            age INTEGER NOT NULL
          )
        """)
      } finally {
        stmt.close()
      }
    }
  }

  private def insertData(
      conn: Connection,
      data: List[TransformedPerson]
  ): F[Unit] = {
    Sync[F].delay {
      val sql = "INSERT INTO people (full_name, age) VALUES (?, ?)"
      val pstmt = conn.prepareStatement(sql)
      try {
        data.foreach { person =>
          pstmt.setString(1, person.fullName)
          pstmt.setInt(2, person.age)
          pstmt.addBatch()
        }
        pstmt.executeBatch()
      } finally {
        pstmt.close()
      }
    }
  }
}

object SQLiteLoader {
  def apply[F[_]: Sync]: SQLiteLoader[F] = new SQLiteLoaderImpl[F]
}

