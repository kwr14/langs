package uk.sky.kurate

import core._
import cats.effect._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.ember.server.EmberServerBuilder
import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.comcast.ip4s._

object ServerComponent extends IOApp {

  // JSON codecs
  implicit val idCodec: JsonValueCodec[ID] = JsonCodecMaker.make
  implicit val statusCodec: JsonValueCodec[core.Status] = JsonCodecMaker.make
  implicit val taskCodec: JsonValueCodec[Task] = JsonCodecMaker.make
  implicit val workflowCodec: JsonValueCodec[Workflow] = JsonCodecMaker.make
  implicit val taskResultCodec: JsonValueCodec[TaskResult] = JsonCodecMaker.make
  implicit val workflowResultCodec: JsonValueCodec[WorkflowResult] = JsonCodecMaker.make
  implicit val taskDefinitionCodec: JsonValueCodec[TaskDefinition] = JsonCodecMaker.make
  implicit val workflowDefinitionCodec: JsonValueCodec[WorkflowDefinition] = JsonCodecMaker.make

  def routes(workflowEngine: WorkflowEngine, persistenceLayer: PersistenceLayer): HttpRoutes[IO] = {
    HttpRoutes.of[IO] {
      case req @ POST -> Root / "workflows" =>
        for {
          workflowDef <- req.decodeJson[WorkflowDefinition]
          variables <- req.params.get("variables").traverse(s => IO.fromEither(readFromString[Map[String, Any]](s)))
          workflow <- IO.fromFuture(IO(workflowEngine.startWorkflow(workflowDef, variables.getOrElse(Map.empty))))
          resp <- Ok(writeToString(workflow))
        } yield resp

      case GET -> Root / "workflows" / UUIDVar(id) =>
        for {
          maybeWorkflow <- IO.fromFuture(IO(persistenceLayer.getWorkflow(id)))
          resp <- maybeWorkflow match {
            case Some(workflow) => Ok(writeToString(workflow))
            case None => NotFound(s"Workflow $id not found")
          }
        } yield resp

      case GET -> Root / "workflows" / UUIDVar(id) / "result" =>
        for {
          maybeResult <- IO.fromFuture(IO(persistenceLayer.getWorkflowResult(id)))
          resp <- maybeResult match {
            case Some(result) => Ok(writeToString(result))
            case None => NotFound(s"Workflow result for $id not found")
          }
        } yield resp
    }
  }

  def run(args: List[String]): IO[ExitCode] = {
    val persistenceLayer = new InMemoryPersistence()
    val workflowEngine = new WorkflowEngine(persistenceLayer)(scala.concurrent.ExecutionContext.global)

    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(routes(workflowEngine, persistenceLayer).orNotFound)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
