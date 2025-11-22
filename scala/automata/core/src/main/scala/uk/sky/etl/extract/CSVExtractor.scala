package uk.sky.etl.extract

import cats.effect.Sync
import cats.syntax.all._
import com.github.tototoshi.csv._
import uk.sky.etl.models.Person
import java.io.File

/**
 * Trait for extracting data from CSV files.
 */
trait CSVExtractor[F[_]] {
  def extract(filePath: String): F[List[Person]]
}

/**
 * Implementation of CSVExtractor using scala-csv library.
 */
class CSVExtractorImpl[F[_]: Sync] extends CSVExtractor[F] {

  override def extract(filePath: String): F[List[Person]] = {
    Sync[F].delay {
      val reader = CSVReader.open(new File(filePath))
      try {
        val rows = reader.allWithHeaders()
        rows.map { row =>
          Person(
            firstName = row.getOrElse("first_name", ""),
            lastName = row.getOrElse("last_name", ""),
            age = row.getOrElse("age", "0").toInt
          )
        }
      } finally {
        reader.close()
      }
    }
  }
}

object CSVExtractor {
  def apply[F[_]: Sync]: CSVExtractor[F] = new CSVExtractorImpl[F]
}

