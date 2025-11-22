package uk.sky.etl

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.io.{File, PrintWriter}
import java.nio.file.Files
import java.sql.DriverManager
import scala.io.Source

class ETLWorkflowSpec extends AnyFlatSpec with Matchers {

  "ETLWorkflow" should "execute complete ETL pipeline" in {
    // Create input CSV
    val inputFile = createTempCSV(
      """first_name,last_name,age
        |John,Doe,30
        |Jane,Smith,25
        |Michael,Johnson,45""".stripMargin
    )

    val outputCsvFile = Files.createTempFile("output", ".csv").toFile
    val dbFile = Files.createTempFile("test", ".db").toFile

    val workflow = ETLWorkflow[IO]

    // Run the workflow
    workflow
      .run(
        inputFile.getAbsolutePath,
        outputCsvFile.getAbsolutePath,
        dbFile.getAbsolutePath
      )
      .unsafeRunSync()

    // Verify CSV output
    val csvContent = Source.fromFile(outputCsvFile).getLines().toList
    csvContent should have length 4 // header + 3 data rows
    csvContent.head shouldBe "full_name,age"
    csvContent(1) shouldBe "John Doe,30"
    csvContent(2) shouldBe "Jane Smith,25"
    csvContent(3) shouldBe "Michael Johnson,45"

    // Verify SQLite output
    val conn = DriverManager.getConnection(s"jdbc:sqlite:${dbFile.getAbsolutePath}")
    try {
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery("SELECT COUNT(*) as count FROM people")
      rs.next() shouldBe true
      rs.getInt("count") shouldBe 3

      rs.close()
      stmt.close()
    } finally {
      conn.close()
    }

    // Cleanup
    inputFile.delete()
    outputCsvFile.delete()
    dbFile.delete()
  }

  it should "handle errors gracefully" in {
    val workflow = ETLWorkflow[IO]

    val result = workflow
      .run(
        "nonexistent.csv",
        "output.csv",
        "test.db"
      )
      .attempt
      .unsafeRunSync()

    result.isLeft shouldBe true
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

