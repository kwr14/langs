package io.iduce

import cats.effect.{ExitCode, IO, IOApp, Resource}
import org.slf4j.LoggerFactory
import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}

object Main extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    // Configure logging
    val rootLogger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)

    rootLogger.info("Starting application...")

    for
      result <- new demo.DurableTaskImpl(
        new demo.SQLiteTaskStateDatabaseService, "taskId-1"
      )
        .run()
      _ <- demo.Logging.logInfo(s"Task result: ${result}")
      _ <- demo.Logging.logInfo("Done")
    yield ExitCode.Success
