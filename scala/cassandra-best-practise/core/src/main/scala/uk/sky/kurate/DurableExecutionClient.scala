import cats.effect._
import org.http4s._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.client.Client
import org.http4s.implicits._
import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import java.util.UUID

class DurableExecutionClient[F[_]: Async](baseUrl: String, httpClient: Client[F]) {
  // JSON codecs
  implicit val idCodec: JsonValueCodec[UUID] = JsonCodecMaker.make
  implicit val statusCodec: JsonValueCodec[Status] = JsonCodecMaker.make
  implicit val taskCodec: JsonValueCodec[Task] = JsonCodecMaker.make
  implicit val workflowCodec: JsonValueCodec[Workflow] = JsonCodecMaker.make
  implicit val taskResultCodec: JsonValueCodec[TaskResult] = JsonCodecMaker.make
  implicit val workflowResultCodec: JsonValueCodec[WorkflowResult] = JsonCodecMaker.make
  implicit val taskDefinitionCodec: JsonValueCodec[TaskDefinition] = JsonCodecMaker.make
  implicit val workflowDefinitionCodec: JsonValueCodec[WorkflowDefinition] = JsonCodecMaker.make

  private val baseUri = Uri.unsafeFromString(baseUrl)

  def startWorkflow(workflowDef: WorkflowDefinition, variables: Map[String, Any]): F[Workflow] = {
    val uri = (baseUri / "workflows").withQueryParam("variables", writeToString(variables))
    val request = Request[F](Method.POST, uri).withEntity(writeToString(workflowDef))
    httpClient.expect[String](request).map(readFromString[Workflow])
  }

  def getWorkflow(id: UUID): F[Option[Workflow]] = {
    val uri = baseUri / "workflows" / id.toString
    httpClient.get(uri) { response =>
      response.status match {
        case Status.Ok => response.as[String].map(s => Some(readFromString[Workflow](s)))
        case Status.NotFound => Async[F].pure(None)
        case _ => response.as[String].flatMap(s => Async[F].raiseError(new Exception(s"Unexpected response: $s")))
      }
    }
  }

  def getWorkflowResult(id: UUID): F[Option[WorkflowResult]] = {
    val uri = baseUri / "workflows" / id.toString / "result"
    httpClient.get(uri) { response =>
      response.status match {
        case Status.Ok => response.as[String].map(s => Some(readFromString[WorkflowResult](s)))
        case Status.NotFound => Async[F].pure(None)
        case _ => response.as[String].flatMap(s => Async[F].raiseError(new Exception(s"Unexpected response: $s")))
      }
    }
  }
}

object DurableExecutionClient {
  def resource[F[_]: Async](baseUrl: String): Resource[F, DurableExecutionClient[F]] =
    EmberClientBuilder.default[F].build.map(client => new DurableExecutionClient[F](baseUrl, client))
}

// Example usage
object ClientExample extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    DurableExecutionClient.resource[IO]("http://localhost:8080").use { client =>
      for {
        // Define a simple workflow
        workflowDef <- IO.pure(WorkflowDefinition(
          name = "Example Workflow",
          taskDefinitions = List(
            TaskDefinition("Task 1", "exampleType", Map.empty, 3, Set.empty),
            TaskDefinition("Task 2", "exampleType", Map.empty, 3, Set("Task 1"))
          )
        ))

        // Start the workflow
        workflow <- client.startWorkflow(workflowDef, Map("key" -> "value"))
        _ <- IO.println(s"Started workflow: ${workflow.id}")

        // Check workflow status
        maybeWorkflow <- client.getWorkflow(workflow.id)
        _ <- IO.println(s"Workflow status: ${maybeWorkflow.map(_.status)}")

        // Get workflow result (it might not be ready yet)
        maybeResult <- client.getWorkflowResult(workflow.id)
        _ <- IO.println(s"Workflow result: ${maybeResult.map(_.taskResults.size)}")
      } yield ExitCode.Success
    }
  }
