package cass4io.domain.movie.model

import java.time.Instant
import com.datastax.oss.driver.api.core.cql.Row
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
  def parse(row: Row): Movie =
    Movie(
      isbn = row.getString("isbn"),
      releasedate = row.getInstant("releasedate"),
      title = row.getString("title"),
      partner = Try(row.getString("partner")).toOption,
      reprint = Try(row.getInstant("reprint")).toOption
    )
}
