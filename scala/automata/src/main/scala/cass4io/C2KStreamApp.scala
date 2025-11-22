package cass4io

import cass4io.domain.movie.model.Movie
import cass4io.domain.movie.tasks.MovieFetcher
import cass4io.domain.movie.tasks.MovieProducer
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.ResultSet
import java.sql.{Connection, DriverManager, PreparedStatement}

object C2KStreamApp extends App {

  // SQLite connection setup
  val sqliteUrl = "jdbc:sqlite:movies.db"
  val connection: Connection = DriverManager.getConnection(sqliteUrl)
  createTable(connection)

  val session: CqlSession = CqlSession.builder().build()
  val movieFetcher = new MovieFetcher(session)
  val movieProducer = new MovieProducer("localhost:9092")

  try {
    val resultSet: ResultSet = movieFetcher.fetchMovies

    resultSet.forEach { row =>
      val movieOpt: Option[Movie] = Movie.parse(row)
      movieOpt.foreach { movie =>
        logStatus(connection, movie.isbn, "inprogress")
        movieProducer.sendMovie("movies_topic", movie)
        logStatus(connection, movie.isbn, "finished")
      }
    }
  } finally {
    movieProducer.close()
    session.close()
    connection.close()
  }

  // Create SQLite table for movie status
  def createTable(connection: Connection): Unit = {
    val statement = connection.createStatement()
    statement.execute(
      "CREATE TABLE IF NOT EXISTS movie_status (id TEXT PRIMARY KEY, status TEXT)"
    )
    statement.close()
  }

  // Log movie status to SQLite
  def logStatus(
      connection: Connection,
      movieId: String,
      status: String
  ): Unit = {
    val preparedStatement: PreparedStatement = connection.prepareStatement(
      "INSERT OR REPLACE INTO movie_status (id, status) VALUES (?, ?)"
    )
    preparedStatement.setString(1, movieId)
    preparedStatement.setString(2, status)
    preparedStatement.executeUpdate()
    preparedStatement.close()
  }
}
