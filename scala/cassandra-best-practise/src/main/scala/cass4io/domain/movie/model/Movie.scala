package cass4io.domain.movie.model

import java.time.Instant
import com.datastax.oss.driver.api.core.cql.Row
import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._

import scala.util.Try

case class FieldInfo(name: String, typ: String, value: Any)

trait RecordMetadata {
  def fieldsInfo: List[FieldInfo]
  def tableName: String
  def primaryKey: String = "isbn"
}

final case class ColumnInfo(
    columnName: String,
    columnType: Any,
    columnValue: Any
)

case class Movie(
    isbn: String,
    releasedate: Instant,
    title: String,
    partner: Option[String],
    reprint: Option[Instant]
) extends RecordMetadata {

  override def fieldsInfo: List[FieldInfo] =
    List(
      FieldInfo("isbn", "String", isbn),
      FieldInfo("releasedate", "Instant", releasedate),
      FieldInfo("title", "String", title),
      FieldInfo("partner", "Option[String]", partner),
      FieldInfo("reprint", "Option[Instant]", reprint)
    )

  override def tableName: String = "movies"

}
object Movie {

  implicit val codec: JsonValueCodec[Movie] = JsonCodecMaker.make

  def safeRead[T](value: T): Option[T] = {
    if (value == null) None
    else Some(value)
  }

  def parse(row: Row): Option[Movie] =
    Try(
      Movie(
        isbn = row.getString("isbn"),
        releasedate = row.getInstant("releasedate"),
        title = row.getString("title"),
        partner = safeRead(row.getString("partner")),
        reprint = safeRead(row.getInstant("reprint"))
      )
    ).toOption
}

object MovieApp extends App {
  import com.github.plokhotnyuk.jsoniter_scala.core._
  import com.github.plokhotnyuk.jsoniter_scala.macros._

  val movie = Movie(
    isbn = "1234567890",
    releasedate = Instant.now(),
    title = "Test Movie",
    partner = Some("Test Partner"),
    reprint = Some(Instant.now())
  )

  // Serialize Movie to JSON
  val json: String = writeToString(movie)
  println(s"Serialized JSON: $json")

  // Deserialize JSON to Movie
  val deserializedMovie: Movie = readFromString[Movie](json)
  println(s"Deserialized Movie: $deserializedMovie")
}
