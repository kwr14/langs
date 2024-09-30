package cass4io

import cass4io.CassApp.CommonCtx
import cass4io.domain.movie.model.Movie
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

  private def generateRandomMovie(id: Int): Movie = {
    val random = new scala.util.Random()
    Movie(
      isbn = id.toString,
      releasedate = Instant
        .now()
        .minusSeconds(random.nextInt(31536000)), // Random date within last year
      title = s"Random Movie ${id}",
      partner =
        if (random.nextBoolean()) Some(s"Partner ${random.nextInt(100)}")
        else None,
      reprint =
        if (random.nextBoolean())
          Some(Instant.now().minusSeconds(random.nextInt(15768000)))
        else None // Random date within last 6 months
    )
  }

  def populateRandomMovies(count: Int): Unit = {
    println(s"Populating $count random movies...")
    (1 to count).foreach { id =>
      val movie = generateRandomMovie(id)
      cassService.safeInsert("movies", movie)
      if (id % 10 == 0) println(s"Inserted $id movies")
    }
    println("Finished populating random movies.")
  }

  // Populate 1 million random movies
  populateRandomMovies(10)

  session.close()
}
