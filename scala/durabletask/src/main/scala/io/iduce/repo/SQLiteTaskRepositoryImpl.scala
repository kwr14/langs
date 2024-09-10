package io.iduce.demo

import cats.effect.IO
import java.sql.{Connection, DriverManager}
import io.iduce.repo.TaskRepository
import cats.effect.{IO, Resource}
import java.sql.{Connection, DriverManager}

// Define a context function for database connections
def withConnection[A](op: Connection => IO[A]): IO[A] = {
  val dbUrl = "jdbc:sqlite:durable_task.db"
  val connectionResource: Resource[IO, Connection] = Resource.make {
    IO(DriverManager.getConnection(dbUrl))
  } { conn =>
    IO(conn.close()).handleErrorWith(_ => IO.unit)
  }
  connectionResource.use(op)
}

class SQLiteTaskStateDatabaseService extends TaskRepository {
  override def persistState(taskId: String, state: TaskState): IO[Unit] =
    withConnection { conn =>
      val sql = """
      INSERT INTO durable_task (task_id, state_data, updateDate) VALUES (?, ?, CURRENT_TIMESTAMP)
      ON CONFLICT(task_id) DO UPDATE SET state_data = excluded.state_data, updateDate = CURRENT_TIMESTAMP
    """
      IO {
        val stmt = conn.prepareStatement(sql)
        try {
          stmt.setString(1, taskId)
          stmt.setString(2, state.state)
          stmt.executeUpdate()
        } finally {
          stmt.close()
        }
      }.map(_ => ())
        .handleErrorWith { error =>
          Logging.logError("Error persisting state", error)
          IO.raiseError(error)
        }
    }

  override def retrieveState(taskId: String): IO[Option[TaskState]] =
    withConnection { conn =>
      val sql = "SELECT state_data FROM durable_task WHERE task_id = ?"
      IO {
        val stmt = conn.prepareStatement(sql)
        try {
          stmt.setString(1, taskId)
          val rs = stmt.executeQuery()
          if (rs.next()) Some(TaskState(rs.getString("state_data")))
          else None
        } finally {
          stmt.close()
        }
      }.handleErrorWith { error =>
        Logging.logError("Error retrieving state", error)
        IO.pure(None)
      }
    }

  override def createTableIfNotExists(): IO[Unit] =
    withConnection { conn =>
      val sql = """
      CREATE TABLE IF NOT EXISTS durable_task (
      task_id TEXT PRIMARY KEY,
      state_data TEXT NOT NULL,
      createDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updateDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    """
      IO {
        val stmt = conn.createStatement()
        try {
          stmt.executeUpdate(sql)
        } finally {
          stmt.close()
        }
      }.map(_ => ())
        .handleErrorWith { error =>
          Logging.logError("Error creating table", error)
          IO.raiseError(error)
        }

    }
}
