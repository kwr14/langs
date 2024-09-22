package cass4io

import cass4io.CassApp.CommonCtx
import com.datastax.oss.driver.api.core.CqlSession
import scala.concurrent.ExecutionContext
import com.datastax.oss.driver.api.core.ConsistencyLevel
import java.time.Instant
import cass4io.domain.movie.model.Movie
import com.datastax.oss.driver.api.core.cql.ResultSet

object C2KStreamApp extends App {

  import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
  import java.util.Properties

  def streamMoviesToKafka(topic: String): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put(
      "key.serializer",
      "org.apache.kafka.common.serialization.StringSerializer"
    )
    props.put(
      "value.serializer",
      "org.apache.kafka.common.serialization.StringSerializer"
    )

    val producer = new KafkaProducer[String, String](props)

    val session: CqlSession = CqlSession.builder().build()
    val selectStmt = session.prepare("SELECT * FROM cassandra_ref.movies")
    val resultSet: ResultSet = session.execute(selectStmt.bind())

    resultSet.forEach { row =>
      val movie = Movie.parse(row)
      val movieJson = s"""{
        "isbn": "${movie.isbn}",
        "releasedate": "${movie.releasedate}",
        "title": "${movie.title}",
        "partner": "${movie.partner.getOrElse("")}",
        "reprint": "${movie.reprint.getOrElse("")}"
      }"""
      val record =
        new ProducerRecord[String, String](topic, movie.isbn, movieJson)
      producer.send(record)
    }

    producer.close()
    session.close()
  }

  // Example usage
  streamMoviesToKafka("movies_topic")

}
