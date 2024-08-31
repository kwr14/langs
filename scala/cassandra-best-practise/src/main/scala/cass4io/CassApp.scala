package cass4io

import cass4io.CassApp.CommonCtx
import com.datastax.oss.driver.api.core.CqlSession
import scala.concurrent.ExecutionContext
import com.datastax.oss.driver.api.core.ConsistencyLevel
import java.time.Instant

object CassApp extends App {
  case class CommonCtx(
      keyspace: String,
      consistencyLevel: ConsistencyLevel,
      ec: Option[ExecutionContext] = None
  )

  case class Movie(
      isbn: String,
      releasedate: Instant,
      title: String,
      partner: Option[String],
      reprint: Option[Instant]
  )

  // Create a CqlSession instance to connect to Cassandra
  val session: CqlSession = CqlSession.builder().build()
  private val cassService: LiveCass4IO = LiveCass4IO(session)

  implicit val ec: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
  val consistencyLevel: ConsistencyLevel = ConsistencyLevel.LOCAL_QUORUM

  implicit val ctx: CommonCtx = CommonCtx(
    keyspace = "cassandra_ref",
    consistencyLevel = consistencyLevel,
    ec = Some(ec)
  )

  val pulp: Movie = Movie(
    isbn = "1",
    releasedate = Instant.parse("2023-07-01T00:00:00Z"),
    title = "Pulp Fiction",
    partner = Some("Sky"),
    reprint = Some(Instant.parse("2023-07-01T00:00:00Z"))
  )

  val pulpNull: Movie = pulp.copy(reprint = None).copy(partner = None)

  // Insert movies
  cassService.safeInsert("movies", pulpNull)
  cassService.safeInsert("movies", pulp.copy(isbn = "2"))

  // Update example: Change the partner of the movie with isbn "1"
  val updatedPulp = pulp.copy(partner = Some("NowTV"))
  try {
    cassService.safeUpdate(
      table = "movies",
      row = updatedPulp,
      primaryKey = "1"
    )
  } catch {
    case e =>
      println(s"plutus: ${e.getMessage()}")
  }

  val updatedPulpNull = pulpNull.copy(partner = Some("Boku"))

  try {
    cassService.safeUpdate(
      table = "movies",
      row = updatedPulpNull,
      primaryKey = "1"
    )
  } catch {
    case e =>
      println(s"plutus: ${e.getMessage()}")
  }

  session.close()
}
