import cats.effect._
import cats.implicits._
import fs2.Stream
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import scala.concurrent.duration._
import java.util.UUID

trait TaskExecutor {
  def execute(task: Task): IO[TaskResult]
}

class Worker[F[_]: Async](
  client: DurableExecutionClient[F],
  taskExecutor: TaskExecutor,
  pollingInterval: FiniteDuration = 5.seconds,
  concurrency: Int = 4
) {
  implicit val logger: Logger[F] = Slf4jLogger.getLogger[F]

  def start: F[Unit] = {
    val process = Stream.awakeEvery[F](pollingInterval)
      .evalMap(_ => pollAndExecuteTasks)
      .parEvalMapUnordered(concurrency)(identity)
      .repeat
      .onError { case e => Stream.eval(Logger[F].error(e)("Error in worker process")) }

    process.compile.drain
  }

  private def pollAndExecuteTasks: F[F[Unit]] = {
    client.getNextTask.flatMap {
      case Some(task) => Async[F].delay(() => executeTask(task))
      case None => Async[F].pure(() => Async[F].unit)
    }
  }

  private def executeTask(task: Task): F[Unit] = {
    for {
      _ <- Logger[F].info(s"Executing task: ${task.id}")
      result <- Async[F].fromIO(taskExecutor.execute(task))
      _ <- client.submitTaskResult(task.id, result)
      _ <- Logger[F].info(s"Task ${task.id} completed with status: ${result.status}")
    } yield ()
  }
}

// Example TaskExecutor implementation
class ExampleTaskExecutor extends TaskExecutor {
  def execute(task: Task): IO[TaskResult] = {
    // Simulate task execution
    IO.sleep(1.second) *> IO {
      TaskResult(
        taskId = task.id,
        status = Completed,
        output = s"Executed ${task.name} of type ${task.taskType}"
      )
    }
  }
}

// Worker companion object for easy instantiation
object Worker {
  def create[F[_]: Async](
    client: DurableExecutionClient[F],
    taskExecutor: TaskExecutor,
    pollingInterval: FiniteDuration = 5.seconds,
    concurrency: Int = 4
  ): Worker[F] = new Worker[F](client, taskExecutor, pollingInterval, concurrency)
}

// Example usage
object WorkerExample extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    DurableExecutionClient.resource[IO]("http://localhost:8080").use { client =>
      val taskExecutor = new ExampleTaskExecutor()
      val worker = Worker.create[IO](client, taskExecutor)

      for {
        _ <- IO.println("Starting worker...")
        fiber <- worker.start.start
        _ <- IO.println("Worker started. Press Enter to stop.")
        _ <- IO.readLine
        _ <- fiber.cancel
        _ <- IO.println("Worker stopped.")
      } yield ExitCode.Success
    }
  }
}
