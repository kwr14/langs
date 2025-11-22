package uk.sky.etl.transform

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import uk.sky.etl.models.{Person, TransformedPerson}

class PersonTransformerSpec extends AnyFlatSpec with Matchers {

  "PersonTransformer" should "transform person to full name format" in {
    val persons = List(
      Person("John", "Doe", 30),
      Person("Jane", "Smith", 25)
    )

    val transformer = PersonTransformer[IO]
    val result = transformer.transform(persons).unsafeRunSync()

    result should have length 2
    result.head shouldBe TransformedPerson("John Doe", 30)
    result(1) shouldBe TransformedPerson("Jane Smith", 25)
  }

  it should "handle single-word names" in {
    val persons = List(
      Person("Madonna", "", 65),
      Person("", "Cher", 77)
    )

    val transformer = PersonTransformer[IO]
    val result = transformer.transform(persons).unsafeRunSync()

    result should have length 2
    result.head shouldBe TransformedPerson("Madonna", 65)
    result(1) shouldBe TransformedPerson("Cher", 77)
  }

  it should "handle empty names" in {
    val persons = List(Person("", "", 0))

    val transformer = PersonTransformer[IO]
    val result = transformer.transform(persons).unsafeRunSync()

    result should have length 1
    result.head shouldBe TransformedPerson("", 0)
  }

  it should "trim whitespace from names" in {
    val persons = List(Person("  John  ", "  Doe  ", 30))

    val transformer = PersonTransformer[IO]
    val result = transformer.transform(persons).unsafeRunSync()

    result should have length 1
    result.head shouldBe TransformedPerson("John Doe", 30)
  }
}

