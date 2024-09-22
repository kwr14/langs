//> using scala 3.5.0
//> using jvm 22
//> using dep "ch.epfl.lamp::gears::0.2.0"
//> using nativeVersion "0.5.1"
//> using dep "org.typelevel::cats-core:2.12.0"

import gears.async.*
import gears.async.default.given
import cats.implicits._
import TaskOrchestrator.*
import gears.async.Async.await

// orchestrator is a service that takes an input chunks the input and submit to tasks
// and waits for the results from each task and continues processing

trait TaskOrchestrator {
  type T
  def orchestrate: Task[List[T]]
}

object TaskOrchestrator {
  case class ChainTaskOrchestrator(tasks: List[Task[Double]])
      extends TaskOrchestrator {
    type T = Double
    def orchestrate: Task[List[Double]] = {
      Task(tasks.map { case task =>
        task.run()
      })
    }
  }
}

@main def main() = {

  def squareTask(n: Int): Task[Double] = Task {
    math.pow(n, 2)
  }
  val numbers = List(1, 2, 3, 4, 5)

  val tasks: List[Task[Double]] = numbers.map(squareTask)

  Async.blocking {

    val taskOrchestrator: ChainTaskOrchestrator =
      ChainTaskOrchestrator(tasks)

    val score: Task[List[Double]] = taskOrchestrator.orchestrate
    println(score.run())

  }
}
