package uk.sky.etl

import cats.effect.Sync
import cats.syntax.all._
import uk.sky.etl.extract.CSVExtractor
import uk.sky.etl.transform.PersonTransformer
import uk.sky.etl.load.{CSVLoader, SQLiteLoader}

/**
 * Trait for orchestrating the ETL workflow.
 */
trait ETLWorkflow[F[_]] {
  def run(inputPath: String, outputCsvPath: String, dbPath: String): F[Unit]
}

/**
 * Implementation of ETLWorkflow that orchestrates extraction, transformation, and loading.
 */
class ETLWorkflowImpl[F[_]: Sync](
    extractor: CSVExtractor[F],
    transformer: PersonTransformer[F],
    csvLoader: CSVLoader[F],
    sqliteLoader: SQLiteLoader[F]
) extends ETLWorkflow[F] {

  override def run(
      inputPath: String,
      outputCsvPath: String,
      dbPath: String
  ): F[Unit] = {
    for {
      _ <- Sync[F].delay(
        println(s"Starting ETL workflow...")
      )
      _ <- Sync[F].delay(
        println(s"Step 1: Extracting data from $inputPath")
      )
      persons <- extractor.extract(inputPath)
      _ <- Sync[F].delay(
        println(s"Extracted ${persons.length} records")
      )

      _ <- Sync[F].delay(println(s"Step 2: Transforming data"))
      transformed <- transformer.transform(persons)
      _ <- Sync[F].delay(
        println(s"Transformed ${transformed.length} records")
      )

      _ <- Sync[F].delay(
        println(s"Step 3: Loading data to CSV at $outputCsvPath")
      )
      _ <- csvLoader.load(transformed, outputCsvPath)
      _ <- Sync[F].delay(println(s"CSV output written successfully"))

      _ <- Sync[F].delay(
        println(s"Step 4: Loading data to SQLite at $dbPath")
      )
      _ <- sqliteLoader.load(transformed, dbPath)
      _ <- Sync[F].delay(println(s"SQLite database populated successfully"))

      _ <- Sync[F].delay(println(s"ETL workflow completed successfully!"))
    } yield ()
  }
}

object ETLWorkflow {
  def apply[F[_]: Sync]: ETLWorkflow[F] = {
    val extractor = CSVExtractor[F]
    val transformer = PersonTransformer[F]
    val csvLoader = CSVLoader[F]
    val sqliteLoader = SQLiteLoader[F]

    new ETLWorkflowImpl[F](extractor, transformer, csvLoader, sqliteLoader)
  }
}

