package uk.sky.etl

import cats.effect.{ExitCode, IO, IOApp}

/**
 * Main application for running the CSV ETL pipeline.
 */
object ETLApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val inputPath = args.headOption.getOrElse("data/input.csv")
    val outputCsvPath = args.lift(1).getOrElse("data/output.csv")
    val dbPath = args.lift(2).getOrElse("data/people.db")

    val workflow = ETLWorkflow[IO]

    workflow
      .run(inputPath, outputCsvPath, dbPath)
      .as(ExitCode.Success)
      .handleErrorWith { error =>
        IO.println(s"ETL workflow failed: ${error.getMessage}") *>
          IO.pure(ExitCode.Error)
      }
  }
}

