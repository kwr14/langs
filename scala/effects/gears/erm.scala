//> using scala "2.13.13"
//> using jvm 22
//> using dep "co.fs2::fs2-core:3.11.0"
//> using dep "org.typelevel::cats-effect:3.5.4"
//> using dep "ch.epfl.lamp::gears::0.2.0"
//> using dep "com.banno::kafka4s:6.0.1"

import cats.effect.IOApp
import cats.effect.IO
import cats.implicits._
import scala.concurrent.duration.DurationInt
import scala.util.Random

object ermApp extends IOApp.Simple {

  val input = fs2.Stream.range(1, 101).covary[IO]

  def filterTask(input: Int): Boolean = {
    input % 2 == 0
  }

  def processTask(input: Int): IO[Int] = {
    def result() = Random.nextInt(1000)
    IO.delay(input + result())
  }

  def produceKafkaTask = ???

  def consumeKafkaTask = ???

  def persistDBTask = ???

  val program = input
    .filter(filterTask)
    .parEvalMap(5)(processTask)

  override def run: IO[Unit] =
    program.debug(x => s"final result: $x").compile.drain

}
