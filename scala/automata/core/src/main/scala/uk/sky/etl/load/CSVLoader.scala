package uk.sky.etl.load

import cats.effect.Sync
import cats.syntax.all._
import com.github.tototoshi.csv._
import uk.sky.etl.models.TransformedPerson
import java.io.File

/**
 * Trait for loading transformed data to CSV files.
 */
trait CSVLoader[F[_]] {
  def load(data: List[TransformedPerson], outputPath: String): F[Unit]
}

/**
 * Implementation of CSVLoader using scala-csv library.
 */
class CSVLoaderImpl[F[_]: Sync] extends CSVLoader[F] {

  override def load(
      data: List[TransformedPerson],
      outputPath: String
  ): F[Unit] = {
    Sync[F].delay {
      val writer = CSVWriter.open(new File(outputPath))
      try {
        // Write header
        writer.writeRow(List("full_name", "age"))

        // Write data rows
        data.foreach { person =>
          writer.writeRow(List(person.fullName, person.age.toString))
        }
      } finally {
        writer.close()
      }
    }
  }
}

object CSVLoader {
  def apply[F[_]: Sync]: CSVLoader[F] = new CSVLoaderImpl[F]
}

