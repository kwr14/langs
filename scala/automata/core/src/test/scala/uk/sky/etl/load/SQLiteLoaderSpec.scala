package uk.sky.etl.load

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import uk.sky.etl.models.TransformedPerson
import java.nio.file.Files
import java.sql.DriverManager

class SQLiteLoaderSpec extends AnyFlatSpec with Matchers {

  "SQLiteLoader" should "create table and insert records" in {
    val data = List(
      TransformedPerson("John Doe", 30),
      TransformedPerson("Jane Smith", 25)
    )

    val tempDb = Files.createTempFile("test", ".db").toFile
    val loader = SQLiteLoader[IO]

    loader.load(data, tempDb.getAbsolutePath).unsafeRunSync()

    // Verify data was inserted
    val conn = DriverManager.getConnection(s"jdbc:sqlite:${tempDb.getAbsolutePath}")
    try {
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery("SELECT full_name, age FROM people ORDER BY full_name")

      rs.next() shouldBe true
      rs.getString("full_name") shouldBe "Jane Smith"
      rs.getInt("age") shouldBe 25

      rs.next() shouldBe true
      rs.getString("full_name") shouldBe "John Doe"
      rs.getInt("age") shouldBe 30

      rs.next() shouldBe false

      rs.close()
      stmt.close()
    } finally {
      conn.close()
    }

    tempDb.delete()
  }

  it should "append to existing table" in {
    val tempDb = Files.createTempFile("test", ".db").toFile
    val loader = SQLiteLoader[IO]

    // First load
    val data1 = List(TransformedPerson("John Doe", 30))
    loader.load(data1, tempDb.getAbsolutePath).unsafeRunSync()

    // Second load
    val data2 = List(TransformedPerson("Jane Smith", 25))
    loader.load(data2, tempDb.getAbsolutePath).unsafeRunSync()

    // Verify both records exist
    val conn = DriverManager.getConnection(s"jdbc:sqlite:${tempDb.getAbsolutePath}")
    try {
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery("SELECT COUNT(*) as count FROM people")
      rs.next() shouldBe true
      rs.getInt("count") shouldBe 2

      rs.close()
      stmt.close()
    } finally {
      conn.close()
    }

    tempDb.delete()
  }

  it should "handle empty data list" in {
    val data = List.empty[TransformedPerson]
    val tempDb = Files.createTempFile("test", ".db").toFile
    val loader = SQLiteLoader[IO]

    loader.load(data, tempDb.getAbsolutePath).unsafeRunSync()

    // Verify table exists but is empty
    val conn = DriverManager.getConnection(s"jdbc:sqlite:${tempDb.getAbsolutePath}")
    try {
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery("SELECT COUNT(*) as count FROM people")
      rs.next() shouldBe true
      rs.getInt("count") shouldBe 0

      rs.close()
      stmt.close()
    } finally {
      conn.close()
    }

    tempDb.delete()
  }
}

