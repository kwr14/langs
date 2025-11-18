package com.github.actions.cli

import cats.effect.kernel.Async
import cats.effect.std.Console
import cats.syntax.all.*
import com.github.actions.client.{GitHubClient, Http4sGitHubClient}
import com.github.actions.ui.{Dashboard, Terminal, KeyReader}
import com.github.actions.domain.{RunFilter, WorkflowStatus}
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.client.Client
import scala.concurrent.duration.*
import io.circe.syntax.*
import io.circe.Encoder

/** Execute CLI commands */
class CommandExecutor[F[_]: Async: Console](config: CliConfig):

  /** Execute a command */
  def execute(cmd: Command): F[Unit] =
    cmd match
      case Command.Dashboard(owner, repo, autoRefresh, interval) =>
        runDashboard(owner, repo, autoRefresh, interval)

      case Command.List(owner, repo, status, branch, limit) =>
        listWorkflowRuns(owner, repo, status, branch, limit)

      case Command.Show(owner, repo, runId) =>
        showWorkflowRun(owner, repo, runId)

      case Command.Rerun(owner, repo, runId, failedOnly) =>
        rerunWorkflow(owner, repo, runId, failedOnly)

      case Command.Cancel(owner, repo, runId) =>
        cancelWorkflow(owner, repo, runId)

      case Command.Init =>
        CliConfig.createSampleConfig[F]

      case Command.Version =>
        showVersion

  /** Run interactive dashboard */
  private def runDashboard(
      owner: String,
      repo: String,
      autoRefresh: Boolean,
      interval: Int
  ): F[Unit] =
    EmberClientBuilder.default[F].build.use { client =>
      val terminal = Terminal.console[F]
      val keyReader = KeyReader.console[F]

      for
        gitHubClient <- createGitHubClient(client)
        dashboard <- Dashboard[F](
          gitHubClient,
          terminal,
          keyReader,
          owner,
          repo
        )
        refreshInterval = if autoRefresh then Some(interval.seconds) else None
        _ <- dashboard.run(refreshInterval)
      yield ()
    }

  /** List workflow runs */
  private def listWorkflowRuns(
      owner: String,
      repo: String,
      status: Option[WorkflowStatus],
      branch: Option[String],
      limit: Int
  ): F[Unit] =
    EmberClientBuilder.default[F].build.use { client =>
      val filter = RunFilter(
        status = status,
        branch = branch,
        actor = None
      )

      for
        gitHubClient <- createGitHubClient(client)
        runs <- gitHubClient.listWorkflowRuns(owner, repo, Some(filter))
        limitedRuns = runs.take(limit)
        _ <- printWorkflowRuns(limitedRuns)
      yield ()
    }

  /** Show workflow run details */
  private def showWorkflowRun(
      owner: String,
      repo: String,
      runId: Long
  ): F[Unit] =
    EmberClientBuilder.default[F].build.use { client =>
      for
        gitHubClient <- createGitHubClient(client)
        run <- gitHubClient.getWorkflowRun(owner, repo, runId)
        jobs <- gitHubClient.listWorkflowRunJobs(owner, repo, runId)
        _ <- printWorkflowRunDetail(run, jobs)
      yield ()
    }

  /** Rerun workflow */
  private def rerunWorkflow(
      owner: String,
      repo: String,
      runId: Long,
      failedOnly: Boolean
  ): F[Unit] =
    EmberClientBuilder.default[F].build.use { client =>
      for
        gitHubClient <- createGitHubClient(client)
        _ <-
          if failedOnly then gitHubClient.rerunFailedJobs(owner, repo, runId)
          else gitHubClient.rerunWorkflow(owner, repo, runId)
        _ <- Async[F].delay(println(s"Workflow run $runId rerun requested"))
      yield ()
    }

  /** Cancel workflow run */
  private def cancelWorkflow(
      owner: String,
      repo: String,
      runId: Long
  ): F[Unit] =
    EmberClientBuilder.default[F].build.use { client =>
      for
        gitHubClient <- createGitHubClient(client)
        _ <- gitHubClient.cancelWorkflowRun(owner, repo, runId)
        _ <- Async[F].delay(println(s"Workflow run $runId cancelled"))
      yield ()
    }

  /** Show version */
  private def showVersion: F[Unit] =
    Async[F].delay {
      println("GitHub Actions CLI v0.1.0")
      println("Built with Scala 3.5.0 and Typelevel stack")
    }

  /** Create GitHub client */
  private def createGitHubClient(client: Client[F]): F[GitHubClient[F]] =
    import cats.effect.kernel.Ref
    val clientConfig = GitHubClient.Config(
      token = config.githubToken,
      baseUri = org.http4s.Uri.unsafeFromString(config.apiBaseUrl),
      userAgent = "github-actions-cli/0.1.0"
    )
    Ref.of[F, Option[GitHubClient.RateLimit]](None).map { rateLimitRef =>
      new Http4sGitHubClient[F](client, clientConfig, rateLimitRef)
    }

  /** Print workflow runs (placeholder - will be enhanced) */
  private def printWorkflowRuns(
      runs: List[com.github.actions.domain.WorkflowRun]
  ): F[Unit] =
    Async[F].delay {
      println(s"Found ${runs.length} workflow runs:")
      runs.foreach { run =>
        val status =
          run.conclusion.map(_.toString).getOrElse(run.status.toString)
        println(f"  ${run.id}%10d  ${run.name}%-40s  $status")
      }
    }

  /** Print workflow run detail (placeholder - will be enhanced) */
  private def printWorkflowRunDetail(
      run: com.github.actions.domain.WorkflowRun,
      jobs: List[com.github.actions.domain.Job]
  ): F[Unit] =
    Async[F].delay {
      println(s"Workflow Run: ${run.name}")
      println(s"  ID: ${run.id}")
      println(s"  Status: ${run.status}")
      println(
        s"  Conclusion: ${run.conclusion.map(_.toString).getOrElse("N/A")}"
      )
      println(s"  Branch: ${run.headBranch}")
      println(s"  Jobs: ${jobs.length}")
      jobs.foreach { job =>
        val status =
          job.conclusion.map(_.toString).getOrElse(job.status.toString)
        println(f"    - ${job.name}%-40s  $status")
      }
    }

object CommandExecutor:
  def apply[F[_]: Async: Console](config: CliConfig): CommandExecutor[F] =
    new CommandExecutor[F](config)
