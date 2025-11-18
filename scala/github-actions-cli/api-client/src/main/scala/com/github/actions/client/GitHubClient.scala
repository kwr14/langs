package com.github.actions.client

import cats.effect.kernel.Async
import com.github.actions.domain.*
import org.http4s.Uri

/** Algebra defining GitHub API operations for workflow runs.
  *
  * This trait provides an abstract interface for interacting with GitHub's
  * Actions API, enabling testability through dependency injection and allowing
  * for multiple implementations (real HTTP client, mock, etc.).
  */
trait GitHubClient[F[_]]:

  /** List workflow runs for a repository.
    *
    * @param owner
    *   Repository owner (user or organization)
    * @param repo
    *   Repository name
    * @param filter
    *   Optional filter criteria
    * @return
    *   List of workflow runs matching the filter
    */
  def listWorkflowRuns(
      owner: String,
      repo: String,
      filter: Option[RunFilter] = None
  ): F[List[WorkflowRun]]

  /** Get details of a specific workflow run.
    *
    * @param owner
    *   Repository owner
    * @param repo
    *   Repository name
    * @param runId
    *   Workflow run ID
    * @return
    *   Workflow run details
    */
  def getWorkflowRun(
      owner: String,
      repo: String,
      runId: Long
  ): F[WorkflowRun]

  /** List jobs for a workflow run.
    *
    * @param owner
    *   Repository owner
    * @param repo
    *   Repository name
    * @param runId
    *   Workflow run ID
    * @return
    *   List of jobs in the workflow run
    */
  def listWorkflowRunJobs(
      owner: String,
      repo: String,
      runId: Long
  ): F[List[Job]]

  /** Rerun a workflow run.
    *
    * @param owner
    *   Repository owner
    * @param repo
    *   Repository name
    * @param runId
    *   Workflow run ID
    * @return
    *   Unit on success
    */
  def rerunWorkflow(
      owner: String,
      repo: String,
      runId: Long
  ): F[Unit]

  /** Rerun failed jobs in a workflow run.
    *
    * @param owner
    *   Repository owner
    * @param repo
    *   Repository name
    * @param runId
    *   Workflow run ID
    * @return
    *   Unit on success
    */
  def rerunFailedJobs(
      owner: String,
      repo: String,
      runId: Long
  ): F[Unit]

  /** Cancel a workflow run.
    *
    * @param owner
    *   Repository owner
    * @param repo
    *   Repository name
    * @param runId
    *   Workflow run ID
    * @return
    *   Unit on success
    */
  def cancelWorkflowRun(
      owner: String,
      repo: String,
      runId: Long
  ): F[Unit]

  /** Get logs for a job.
    *
    * @param owner
    *   Repository owner
    * @param repo
    *   Repository name
    * @param jobId
    *   Job ID
    * @return
    *   Job logs as a string
    */
  def getJobLogs(
      owner: String,
      repo: String,
      jobId: Long
  ): F[String]

object GitHubClient:
  /** Configuration for GitHub API client.
    *
    * @param token
    *   GitHub personal access token
    * @param baseUri
    *   Base URI for GitHub API (default: https://api.github.com)
    * @param userAgent
    *   User-Agent header value
    */
  case class Config(
      token: String,
      baseUri: Uri = Uri.unsafeFromString("https://api.github.com"),
      userAgent: String = "github-actions-cli/0.1.0"
  )

  /** Rate limit information from GitHub API headers.
    *
    * @param limit
    *   Maximum number of requests per hour
    * @param remaining
    *   Number of requests remaining
    * @param reset
    *   Unix timestamp when the rate limit resets
    */
  case class RateLimit(
      limit: Int,
      remaining: Int,
      reset: Long
  )

  /** GitHub API error response.
    *
    * @param message
    *   Error message
    * @param documentationUrl
    *   Optional link to documentation
    */
  case class GitHubError(
      message: String,
      documentationUrl: Option[String] = None
  ) extends Exception(message)
