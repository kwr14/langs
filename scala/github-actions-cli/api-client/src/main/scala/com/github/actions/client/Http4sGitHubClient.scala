package com.github.actions.client

import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import com.github.actions.domain.*
import io.circe.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client.Client
import org.http4s.headers.{Accept, Authorization}
import org.http4s.implicits.*
import org.typelevel.ci.CIStringSyntax
import scala.util.chaining.*

/** HTTP4s-based implementation of GitHubClient.
  *
  * This implementation uses http4s-ember-client to make HTTP requests to
  * GitHub's REST API with proper authentication, error handling, and rate limit
  * tracking.
  */
class Http4sGitHubClient[F[_]: Async: Console](
    client: Client[F],
    config: GitHubClient.Config,
    rateLimitRef: Ref[F, Option[GitHubClient.RateLimit]]
) extends GitHubClient[F]:

  import Http4sGitHubClient.*

  private val authHeader = Authorization(
    Credentials.Token(AuthScheme.Bearer, config.token)
  )
  private val acceptHeader = Accept(MediaType.application.json)
  private val userAgentHeader = Header.Raw(ci"User-Agent", config.userAgent)

  private def buildUri(
      path: String,
      params: Map[String, String] = Map.empty
  ): Uri =
    val segments = path.split("/").filter(_.nonEmpty)
    val base = segments.foldLeft(config.baseUri)(_ / _)
    params.foldLeft(base) { case (uri, (key, value)) =>
      uri.withQueryParam(key, value)
    }

  private def request[A: Decoder](
      method: Method,
      uri: Uri,
      body: Option[Json] = None
  ): F[A] =
    val req = Request[F](method, uri)
      .withHeaders(authHeader, acceptHeader, userAgentHeader)
      .pipe(r => body.fold(r)(j => r.withEntity(j)))

    client.run(req).use { response =>
      // Extract and update rate limit info
      val rateLimit = extractRateLimit(response)
      rateLimitRef.set(rateLimit) *> (
        response.status match
          case Status.Ok =>
            response.as[A]

          case Status.Created | Status.Accepted | Status.NoContent =>
            // These statuses may have empty bodies
            response.contentLength match
              case Some(0) | None =>
                Async[F].pure(().asInstanceOf[A])
              case _ =>
                response.as[A]

          case Status.NotFound =>
            Async[F].raiseError(
              GitHubClient
                .GitHubError(s"Resource not found: ${uri.renderString}")
            )

          case Status.Forbidden | Status.Conflict | Status.TooManyRequests =>
            response.as[GitHubErrorResponse].flatMap { err =>
              Async[F].raiseError(
                GitHubClient.GitHubError(
                  err.message,
                  err.documentation_url
                )
              )
            }

          case _ =>
            response.bodyText.compile.string.flatMap { body =>
              Async[F].raiseError(
                GitHubClient.GitHubError(
                  s"GitHub API error: ${response.status.code} - $body"
                )
              )
            }
      )
    }

  private def extractRateLimit(
      response: Response[F]
  ): Option[GitHubClient.RateLimit] =
    for
      limit <- response.headers
        .get(ci"X-RateLimit-Limit")
        .flatMap(_.head.value.toIntOption)
      remaining <- response.headers
        .get(ci"X-RateLimit-Remaining")
        .flatMap(_.head.value.toIntOption)
      reset <- response.headers
        .get(ci"X-RateLimit-Reset")
        .flatMap(_.head.value.toLongOption)
    yield GitHubClient.RateLimit(limit, remaining, reset)

  override def listWorkflowRuns(
      owner: String,
      repo: String,
      filter: Option[RunFilter]
  ): F[List[WorkflowRun]] =
    val params = filter.fold(Map.empty[String, String]) { f =>
      List(
        f.status.map(s => "status" -> s.toString.toLowerCase),
        f.branch.map(b => "branch" -> b),
        f.actor.map(a => "actor" -> a)
      ).flatten.toMap
    }

    val uri = buildUri(s"repos/$owner/$repo/actions/runs", params)
    request[WorkflowRunsResponse](Method.GET, uri).map(_.workflow_runs)

  override def getWorkflowRun(
      owner: String,
      repo: String,
      runId: Long
  ): F[WorkflowRun] =
    val uri = buildUri(s"repos/$owner/$repo/actions/runs/$runId")
    request[WorkflowRun](Method.GET, uri)

  override def listWorkflowRunJobs(
      owner: String,
      repo: String,
      runId: Long
  ): F[List[Job]] =
    val uri = buildUri(s"repos/$owner/$repo/actions/runs/$runId/jobs")
    request[JobsResponse](Method.GET, uri).map(_.jobs)

  override def rerunWorkflow(
      owner: String,
      repo: String,
      runId: Long
  ): F[Unit] =
    val uri = buildUri(s"repos/$owner/$repo/actions/runs/$runId/rerun")
    request[Unit](Method.POST, uri)

  override def rerunFailedJobs(
      owner: String,
      repo: String,
      runId: Long
  ): F[Unit] =
    val uri = buildUri(
      s"repos/$owner/$repo/actions/runs/$runId/rerun-failed-jobs"
    )
    request[Unit](Method.POST, uri)

  override def cancelWorkflowRun(
      owner: String,
      repo: String,
      runId: Long
  ): F[Unit] =
    val uri = buildUri(s"repos/$owner/$repo/actions/runs/$runId/cancel")
    request[Unit](Method.POST, uri)

  override def getJobLogs(
      owner: String,
      repo: String,
      jobId: Long
  ): F[String] =
    val uri = buildUri(s"repos/$owner/$repo/actions/jobs/$jobId/logs")
    val req = Request[F](Method.GET, uri)
      .withHeaders(authHeader, userAgentHeader)

    // GitHub returns a 302 redirect to the actual log file
    // The client should follow redirects automatically
    client.expect[String](req).handleErrorWith { error =>
      // If logs are not available, return empty string
      Async[F].pure("")
    }

object Http4sGitHubClient:
  // GitHub API response wrappers
  case class WorkflowRunsResponse(workflow_runs: List[WorkflowRun])
  case class JobsResponse(jobs: List[Job])
  case class GitHubErrorResponse(
      message: String,
      documentation_url: Option[String]
  )

  given Decoder[WorkflowRunsResponse] = Decoder.derived
  given Decoder[JobsResponse] = Decoder.derived
  given Decoder[GitHubErrorResponse] = Decoder.derived

  /** Create a new GitHubClient with http4s-ember-client.
    *
    * @param config
    *   GitHub API configuration
    * @return
    *   Resource managing the HTTP client lifecycle
    */
  def resource[F[_]: Async: Console](
      config: GitHubClient.Config
  ): Resource[F, GitHubClient[F]] =
    import org.http4s.ember.client.EmberClientBuilder
    for
      client <- EmberClientBuilder.default[F].build
      rateLimitRef <- Resource.eval(
        Ref.of[F, Option[GitHubClient.RateLimit]](None)
      )
    yield new Http4sGitHubClient[F](client, config, rateLimitRef)
