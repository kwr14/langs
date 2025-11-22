package uk.sky.etl.load

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import uk.sky.etl.models.TransformedPerson
import java.nio.file.Files
import scala.io.Source

class CSVLoaderSpec extends AnyFlatSpec with Matchers {

  "CSVLoader" should "write transformed data to CSV" in {
    val data = List(
      TransformedPerson("John Doe", 30),
      TransformedPerson("Jane Smith", 25)
    )

    val tempFile = Files.createTempFile("output", ".csv").toFile
    val loader = CSVLoader[IO]

    loader.load(data, tempFile.getAbsolutePath).unsafeRunSync()

    val content = Source.fromFile(tempFile).getLines().toList
    content should have length 3 // header + 2 data rows
    content.head shouldBe "full_name,age"
    content(1) shouldBe "John Doe,30"
    content(2) shouldBe "Jane Smith,25"

    tempFile.delete()
  }

  it should "write empty CSV with headers only" in {
    val data = List.empty[TransformedPerson]

    val tempFile = Files.createTempFile("output", ".csv").toFile
    val loader = CSVLoader[IO]

    loader.load(data, tempFile.getAbsolutePath).unsafeRunSync()

    val content = Source.fromFile(tempFile).getLines().toList
    content should have length 1
    content.head shouldBe "full_name,age"

    tempFile.delete()
  }

  it should "handle special characters in names" in {
    val data = List(
      TransformedPerson("O'Brien, John", 30),
      TransformedPerson("Smith-Jones, Mary", 25)
    )

    val tempFile = Files.createTempFile("output", ".csv").toFile
    val loader = CSVLoader[IO]

    loader.load(data, tempFile.getAbsolutePath).unsafeRunSync()

    val content = Source.fromFile(tempFile).getLines().toList
    content should have length 3

    tempFile.delete()
  }
}

