package com.github.actions.client

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.github.actions.domain.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.client.Client
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant

class GitHubClientSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers:

  // Test data
  val testActor =
    Actor("octocat", "https://github.com/images/error/octocat_happy.gif")
  val testRepo = Repository("owner", "repo")

  val testWorkflowRun = WorkflowRun(
    id = 123456789L,
    name = "CI",
    status = WorkflowStatus.Completed,
    conclusion = Some(WorkflowConclusion.Success),
    headBranch = "main",
    headSha = "abc123",
    actor = testActor,
    createdAt = Instant.parse("2023-01-01T00:00:00Z"),
    updatedAt = Instant.parse("2023-01-01T00:05:00Z"),
    runStartedAt = Some(Instant.parse("2023-01-01T00:00:10Z")),
    jobs = List.empty
  )

  val testJob = Job(
    id = 987654321L,
    name = "build",
    status = WorkflowStatus.Completed,
    conclusion = Some(WorkflowConclusion.Success),
    startedAt = Some(Instant.parse("2023-01-01T00:00:15Z")),
    completedAt = Some(Instant.parse("2023-01-01T00:04:00Z")),
    steps = List.empty
  )

  def mockClient(routes: HttpRoutes[IO]): Client[IO] =
    Client.fromHttpApp(routes.orNotFound)

  def createGitHubClient(client: Client[IO]): IO[GitHubClient[IO]] =
    import cats.effect.std.Console
    given Console[IO] = Console.make[IO]

    for
      rateLimitRef <- cats.effect.Ref.of[IO, Option[GitHubClient.RateLimit]](
        None
      )
      config = GitHubClient.Config(token = "test-token")
    yield new Http4sGitHubClient[IO](client, config, rateLimitRef)

  "GitHubClient" - {
    "listWorkflowRuns" - {
      "should successfully list workflow runs" in {
        val routes = HttpRoutes.of[IO] {
          case GET -> Root / "repos" / "owner" / "repo" / "actions" / "runs" =>
            val response = io.circe.Json.obj(
              "workflow_runs" -> List(testWorkflowRun).asJson
            )
            Ok(response)
        }

        val client = mockClient(routes)

        createGitHubClient(client)
          .flatMap { ghClient =>
            ghClient.listWorkflowRuns("owner", "repo", None).map { runs =>
              runs should have size 1
              runs.head.id shouldBe 123456789L
              runs.head.name shouldBe "CI"
              runs.head.status shouldBe WorkflowStatus.Completed
            }
          }
          .asserting(identity)
      }

      "should filter workflow runs by status" in {
        val routes = HttpRoutes.of[IO] {
          case GET -> Root / "repos" / "owner" / "repo" / "actions" / "runs" :?
              StatusQueryParamMatcher(status) =>
            status shouldBe "completed"
            val response = io.circe.Json.obj(
              "workflow_runs" -> List(testWorkflowRun).asJson
            )
            Ok(response)
        }

        val client = mockClient(routes)
        val filter = RunFilter(status = Some(WorkflowStatus.Completed))

        createGitHubClient(client)
          .flatMap { ghClient =>
            ghClient.listWorkflowRuns("owner", "repo", Some(filter)).map {
              runs =>
                runs should have size 1
            }
          }
          .asserting(identity)
      }

      "should handle empty workflow runs list" in {
        val routes = HttpRoutes.of[IO] {
          case GET -> Root / "repos" / "owner" / "repo" / "actions" / "runs" =>
            val response = io.circe.Json.obj(
              "workflow_runs" -> List.empty[WorkflowRun].asJson
            )
            Ok(response)
        }

        val client = mockClient(routes)

        createGitHubClient(client)
          .flatMap { ghClient =>
            ghClient.listWorkflowRuns("owner", "repo", None).map { runs =>
              runs shouldBe empty
            }
          }
          .asserting(identity)
      }
    }

    "getWorkflowRun" - {
      "should successfully get a workflow run" in {
        val routes = HttpRoutes.of[IO] {
          case GET -> Root / "repos" / "owner" / "repo" / "actions" / "runs" / LongVar(
                runId
              ) =>
            runId shouldBe 123456789L
            Ok(testWorkflowRun.asJson)
        }

        val client = mockClient(routes)

        createGitHubClient(client)
          .flatMap { ghClient =>
            ghClient.getWorkflowRun("owner", "repo", 123456789L).map { run =>
              run.id shouldBe 123456789L
              run.name shouldBe "CI"
            }
          }
          .asserting(identity)
      }

      "should handle 404 not found" in {
        val routes = HttpRoutes.of[IO] {
          case GET -> Root / "repos" / "owner" / "repo" / "actions" / "runs" / LongVar(
                _
              ) =>
            NotFound()
        }

        val client = mockClient(routes)

        createGitHubClient(client)
          .flatMap { ghClient =>
            ghClient.getWorkflowRun("owner", "repo", 999L).attempt.map {
              case Left(err: GitHubClient.GitHubError) =>
                err.getMessage should include("not found")
              case _ =>
                fail("Expected GitHubError")
            }
          }
          .asserting(identity)
      }
    }

    "listWorkflowRunJobs" - {
      "should successfully list jobs for a workflow run" in {
        val routes = HttpRoutes.of[IO] {
          case GET -> Root / "repos" / "owner" / "repo" / "actions" / "runs" / LongVar(
                runId
              ) / "jobs" =>
            runId shouldBe 123456789L
            val response = io.circe.Json.obj(
              "jobs" -> List(testJob).asJson
            )
            Ok(response)
        }

        val client = mockClient(routes)

        createGitHubClient(client)
          .flatMap { ghClient =>
            ghClient.listWorkflowRunJobs("owner", "repo", 123456789L).map {
              jobs =>
                jobs should have size 1
                jobs.head.id shouldBe 987654321L
                jobs.head.name shouldBe "build"
            }
          }
          .asserting(identity)
      }

      "should handle empty jobs list" in {
        val routes = HttpRoutes.of[IO] {
          case GET -> Root / "repos" / "owner" / "repo" / "actions" / "runs" / LongVar(
                _
              ) / "jobs" =>
            val response = io.circe.Json.obj(
              "jobs" -> List.empty[Job].asJson
            )
            Ok(response)
        }

        val client = mockClient(routes)

        createGitHubClient(client)
          .flatMap { ghClient =>
            ghClient.listWorkflowRunJobs("owner", "repo", 123456789L).map {
              jobs =>
                jobs shouldBe empty
            }
          }
          .asserting(identity)
      }
    }

    "rerunWorkflow" - {
      "should successfully rerun a workflow" in {
        val routes = HttpRoutes.of[IO] {
          case POST -> Root / "repos" / "owner" / "repo" / "actions" / "runs" / LongVar(
                runId
              ) / "rerun" =>
            runId shouldBe 123456789L
            Created()
        }

        val client = mockClient(routes)

        createGitHubClient(client)
          .flatMap { ghClient =>
            ghClient.rerunWorkflow("owner", "repo", 123456789L).map { _ =>
              succeed
            }
          }
          .asserting(identity)
      }

      "should handle 403 forbidden" in {
        val routes = HttpRoutes.of[IO] {
          case POST -> Root / "repos" / "owner" / "repo" / "actions" / "runs" / LongVar(
                _
              ) / "rerun" =>
            val errorResponse = io.circe.Json.obj(
              "message" -> io.circe.Json
                .fromString("Resource not accessible by integration"),
              "documentation_url" -> io.circe.Json.fromString(
                "https://docs.github.com"
              )
            )
            Forbidden(errorResponse)
        }

        val client = mockClient(routes)

        createGitHubClient(client)
          .flatMap { ghClient =>
            ghClient.rerunWorkflow("owner", "repo", 123456789L).attempt.map {
              case Left(err: GitHubClient.GitHubError) =>
                err.getMessage should include("Resource not accessible")
                err.documentationUrl shouldBe Some("https://docs.github.com")
              case _ =>
                fail("Expected GitHubError")
            }
          }
          .asserting(identity)
      }
    }

    "rerunFailedJobs" - {
      "should successfully rerun failed jobs" in {
        val routes = HttpRoutes.of[IO] {
          case POST -> Root / "repos" / "owner" / "repo" / "actions" / "runs" / LongVar(
                runId
              ) / "rerun-failed-jobs" =>
            runId shouldBe 123456789L
            Created()
        }

        val client = mockClient(routes)

        createGitHubClient(client)
          .flatMap { ghClient =>
            ghClient.rerunFailedJobs("owner", "repo", 123456789L).map { _ =>
              succeed
            }
          }
          .asserting(identity)
      }
    }

    "cancelWorkflowRun" - {
      "should successfully cancel a workflow run" in {
        val routes = HttpRoutes.of[IO] {
          case POST -> Root / "repos" / "owner" / "repo" / "actions" / "runs" / LongVar(
                runId
              ) / "cancel" =>
            runId shouldBe 123456789L
            Accepted()
        }

        val client = mockClient(routes)

        createGitHubClient(client)
          .flatMap { ghClient =>
            ghClient.cancelWorkflowRun("owner", "repo", 123456789L).map { _ =>
              succeed
            }
          }
          .asserting(identity)
      }

      "should handle workflow that cannot be cancelled" in {
        val routes = HttpRoutes.of[IO] {
          case POST -> Root / "repos" / "owner" / "repo" / "actions" / "runs" / LongVar(
                _
              ) / "cancel" =>
            val errorResponse = io.circe.Json.obj(
              "message" -> io.circe.Json.fromString(
                "Cannot cancel a workflow run that is completed"
              )
            )
            Conflict(errorResponse)
        }

        val client = mockClient(routes)

        createGitHubClient(client)
          .flatMap { ghClient =>
            ghClient
              .cancelWorkflowRun("owner", "repo", 123456789L)
              .attempt
              .map {
                case Left(err: GitHubClient.GitHubError) =>
                  err.getMessage should include("Cannot cancel")
                case _ =>
                  fail("Expected GitHubError")
              }
          }
          .asserting(identity)
      }
    }

    "error handling" - {
      "should handle rate limiting" in {
        val routes = HttpRoutes.of[IO] {
          case GET -> Root / "repos" / "owner" / "repo" / "actions" / "runs" =>
            val errorResponse = io.circe.Json.obj(
              "message" -> io.circe.Json.fromString("API rate limit exceeded")
            )
            TooManyRequests(errorResponse)
        }

        val client = mockClient(routes)

        createGitHubClient(client)
          .flatMap { ghClient =>
            ghClient.listWorkflowRuns("owner", "repo", None).attempt.map {
              case Left(err: GitHubClient.GitHubError) =>
                err.getMessage should include("rate limit")
              case _ =>
                fail("Expected GitHubError")
            }
          }
          .asserting(identity)
      }

      "should handle server errors" in {
        val routes = HttpRoutes.of[IO] {
          case GET -> Root / "repos" / "owner" / "repo" / "actions" / "runs" =>
            InternalServerError("Server error")
        }

        val client = mockClient(routes)

        createGitHubClient(client)
          .flatMap { ghClient =>
            ghClient.listWorkflowRuns("owner", "repo", None).attempt.map {
              case Left(err: GitHubClient.GitHubError) =>
                err.getMessage should (include("500") or include(
                  "Server error"
                ))
              case Left(other) =>
                fail(
                  s"Expected GitHubError but got: ${other.getClass.getName}: ${other.getMessage}"
                )
              case Right(_) =>
                fail("Expected error but got success")
            }
          }
          .asserting(identity)
      }
    }
  }

// Query parameter matcher for status
object StatusQueryParamMatcher
    extends QueryParamDecoderMatcher[String]("status")
