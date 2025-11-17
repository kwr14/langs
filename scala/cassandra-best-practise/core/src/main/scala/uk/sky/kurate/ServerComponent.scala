package uk.sky.kurate

import core._
import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.staticcontent.FileService
import org.http4s.headers.{`Content-Type`, Location}
import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.comcast.ip4s._
import java.nio.file.{Files, Paths}
import scala.io.Source
import java.util.UUID
import scala.util.Try

object ServerComponent extends IOApp {

  // JSON codecs
  implicit val idCodec: JsonValueCodec[ID] = JsonCodecMaker.make
  implicit val statusCodec: JsonValueCodec[core.Status] = JsonCodecMaker.make
  implicit val taskCodec: JsonValueCodec[Task] = JsonCodecMaker.make
  implicit val workflowCodec: JsonValueCodec[Workflow] = JsonCodecMaker.make
  implicit val workflowListCodec: JsonValueCodec[List[Workflow]] =
    JsonCodecMaker.make
  implicit val taskResultCodec: JsonValueCodec[TaskResult] = JsonCodecMaker.make
  implicit val workflowResultCodec: JsonValueCodec[WorkflowResult] =
    JsonCodecMaker.make
  implicit val taskDefinitionCodec: JsonValueCodec[TaskDefinition] =
    JsonCodecMaker.make
  implicit val workflowDefinitionCodec: JsonValueCodec[WorkflowDefinition] =
    JsonCodecMaker.make

  // Serve OpenAPI spec
  def apiDocsRoutes: HttpRoutes[IO] = {
    HttpRoutes.of[IO] {
      // Serve the OpenAPI YAML file
      case GET -> Root / "api-docs" / "openapi.yaml" =>
        IO {
          val openapiPath = Paths.get("openapi.yaml")
          if (Files.exists(openapiPath)) {
            val content = Source.fromFile(openapiPath.toFile).mkString
            Response[IO](
              status = Status.Ok,
              headers = Headers(`Content-Type`(MediaType.text.yaml))
            ).withEntity(content)
          } else {
            Response[IO](status = Status.NotFound)
              .withEntity("OpenAPI specification not found")
          }
        }

      // Redirect /api-docs to Swagger UI
      case GET -> Root / "api-docs" =>
        PermanentRedirect(
          Location(
            Uri.unsafeFromString(
              "/api-docs/index.html?url=/api-docs/openapi.yaml"
            )
          )
        )

      // Serve Swagger UI static files from WebJars
      case req @ GET -> "api-docs" /: path =>
        StaticFile
          .fromResource(
            s"/META-INF/resources/webjars/swagger-ui/5.10.3/${path.segments.mkString("/")}",
            Some(req)
          )
          .getOrElseF(NotFound())
    }
  }

  // Workflow management routes
  def workflowRoutes(
      workflowEngine: WorkflowEngine,
      persistenceLayer: PersistenceLayer
  ): HttpRoutes[IO] = {
    HttpRoutes.of[IO] {
      case req @ POST -> Root / "workflows" =>
        for {
          bodyStr <- req.as[String]
          workflowDef <- IO.fromTry(
            Try(readFromString[WorkflowDefinition](bodyStr))
          )
          workflow <- IO.fromFuture(
            IO(
              workflowEngine.startWorkflow(
                workflowDef,
                Map.empty // Simplified - no variables for now
              )
            )
          )
          resp <- Ok(writeToString(workflow))
        } yield resp

      case GET -> Root / "workflows" =>
        for {
          workflows <- IO.fromFuture(IO(persistenceLayer.listWorkflows()))
          resp <- Ok(writeToString(workflows))
        } yield resp

      case GET -> Root / "workflows" / UUIDVar(id) =>
        for {
          maybeWorkflow <- IO.fromFuture(IO(persistenceLayer.getWorkflow(id)))
          resp <- maybeWorkflow match {
            case Some(workflow) => Ok(writeToString(workflow))
            case None           => NotFound(s"Workflow $id not found")
          }
        } yield resp

      case GET -> Root / "workflows" / UUIDVar(id) / "result" =>
        for {
          maybeResult <- IO.fromFuture(
            IO(persistenceLayer.getWorkflowResult(id))
          )
          resp <- maybeResult match {
            case Some(result) => Ok(writeToString(result))
            case None         => NotFound(s"Workflow result for $id not found")
          }
        } yield resp
    }
  }

  // Combine all routes
  def allRoutes(
      workflowEngine: WorkflowEngine,
      persistenceLayer: PersistenceLayer
  ): HttpRoutes[IO] = {
    workflowRoutes(workflowEngine, persistenceLayer) <+> apiDocsRoutes
  }

  def run(args: List[String]): IO[ExitCode] = {
    val persistenceLayer = new InMemoryPersistence()
    val workflowEngine = new WorkflowEngine(persistenceLayer)(
      scala.concurrent.ExecutionContext.global
    )

    val app = allRoutes(workflowEngine, persistenceLayer).orNotFound

    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(app)
      .build
      .use(_ =>
        IO.println("🚀 Server started on http://localhost:8080") *>
          IO.println("📖 API Documentation: http://localhost:8080/api-docs") *>
          IO.println(
            "📝 OpenAPI Spec: http://localhost:8080/api-docs/openapi.yaml"
          ) *>
          IO.never
      )
      .as(ExitCode.Success)
  }
}
