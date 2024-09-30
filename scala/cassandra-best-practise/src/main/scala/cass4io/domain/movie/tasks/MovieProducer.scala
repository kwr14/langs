package cass4io.domain.movie.tasks

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.util.Properties
import cass4io.domain.movie.model.Movie
import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._
import org.apache.kafka.common.header.Header

class MovieProducer(broker: String) {
  private val props = new Properties()
  props.put("bootstrap.servers", broker)
  props.put(
    "key.serializer",
    "org.apache.kafka.common.serialization.StringSerializer"
  )
  props.put(
    "value.serializer",
    "org.apache.kafka.common.serialization.StringSerializer"
  )

  private val producer = new KafkaProducer[String, String](props)

  val header1 = new Header {
    override def key(): String = "project-1"
    override def value(): Array[Byte] = "ITUNES".getBytes()
  }

  val headers: java.lang.Iterable[Header] = new java.lang.Iterable[Header] {
    override def iterator(): java.util.Iterator[Header] =
      java.util.Collections.singleton(header1).iterator()
  }

  def sendMovie(topic: String, movie: Movie): Unit = {
    val movieJson = writeToString[Movie](movie)
    val record =
      new ProducerRecord[String, String](
        topic,
        null,
        null,
        movie.isbn,
        movieJson,
        headers
      )
    producer.send(record)
  }

  def close(): Unit = producer.close()
}
