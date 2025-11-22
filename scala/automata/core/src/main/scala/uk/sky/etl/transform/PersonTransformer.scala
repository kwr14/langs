package uk.sky.etl.transform

import cats.effect.Sync
import cats.syntax.all._
import uk.sky.etl.models.{Person, TransformedPerson}

/**
 * Trait for transforming Person to TransformedPerson.
 */
trait PersonTransformer[F[_]] {
  def transform(persons: List[Person]): F[List[TransformedPerson]]
}

/**
 * Implementation of PersonTransformer that combines first and last names.
 */
class PersonTransformerImpl[F[_]: Sync] extends PersonTransformer[F] {

  override def transform(persons: List[Person]): F[List[TransformedPerson]] = {
    Sync[F].delay {
      persons.map { person =>
        val fullName = combineNames(person.firstName, person.lastName)
        TransformedPerson(fullName = fullName, age = person.age)
      }
    }
  }

  private def combineNames(firstName: String, lastName: String): String = {
    val trimmedFirst = firstName.trim
    val trimmedLast = lastName.trim

    (trimmedFirst, trimmedLast) match {
      case ("", "") => ""
      case (first, "") => first
      case ("", last) => last
      case (first, last) => s"$first $last"
    }
  }
}

object PersonTransformer {
  def apply[F[_]: Sync]: PersonTransformer[F] = new PersonTransformerImpl[F]
}

