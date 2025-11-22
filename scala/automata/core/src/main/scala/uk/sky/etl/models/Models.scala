package uk.sky.etl.models

import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._

/**
 * Represents a person with first name, last name, and age.
 * This is the input model for the ETL pipeline.
 */
case class Person(
    firstName: String,
    lastName: String,
    age: Int
)

/**
 * Represents a transformed person with combined full name and age.
 * This is the output model for the ETL pipeline.
 */
case class TransformedPerson(
    fullName: String,
    age: Int
)

object Models {
  // JSON codecs for Person
  implicit val personCodec: JsonValueCodec[Person] = JsonCodecMaker.make

  // JSON codecs for TransformedPerson
  implicit val transformedPersonCodec: JsonValueCodec[TransformedPerson] =
    JsonCodecMaker.make

  // JSON codecs for lists
  implicit val personListCodec: JsonValueCodec[List[Person]] =
    JsonCodecMaker.make
  implicit val transformedPersonListCodec
      : JsonValueCodec[List[TransformedPerson]] = JsonCodecMaker.make
}

