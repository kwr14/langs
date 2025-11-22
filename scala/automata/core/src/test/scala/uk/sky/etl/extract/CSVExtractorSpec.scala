package uk.sky.etl.extract

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import uk.sky.etl.models.Person
import java.io.{File, PrintWriter}
import java.nio.file.Files

class CSVExtractorSpec extends AnyFlatSpec with Matchers {

  "CSVExtractor" should "extract person data from CSV" in {
    val tempFile = createTempCSV(
      """first_name,last_name,age
        |John,Doe,30
        |Jane,Smith,25""".stripMargin
    )

    val extractor = CSVExtractor[IO]
    val result = extractor.extract(tempFile.getAbsolutePath).unsafeRunSync()

    result should have length 2
    result.head shouldBe Person("John", "Doe", 30)
    result(1) shouldBe Person("Jane", "Smith", 25)

    tempFile.delete()
  }

  it should "handle empty CSV file" in {
    val tempFile = createTempCSV("first_name,last_name,age")

    val extractor = CSVExtractor[IO]
    val result = extractor.extract(tempFile.getAbsolutePath).unsafeRunSync()

    result shouldBe empty

    tempFile.delete()
  }

  it should "handle missing fields with defaults" in {
    val tempFile = createTempCSV(
      """first_name,last_name,age
        |John,,30
        |,Smith,25""".stripMargin
    )

    val extractor = CSVExtractor[IO]
    val result = extractor.extract(tempFile.getAbsolutePath).unsafeRunSync()

    result should have length 2
    result.head shouldBe Person("John", "", 30)
    result(1) shouldBe Person("", "Smith", 25)

    tempFile.delete()
  }

  private def createTempCSV(content: String): File = {
    val tempFile = Files.createTempFile("test", ".csv").toFile
    val writer = new PrintWriter(tempFile)
    try {
      writer.write(content)
    } finally {
      writer.close()
    }
    tempFile
  }
}

