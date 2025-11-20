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
      case Command.Dashboard(owner, repo, autoRefresh, interval, reposOpt) =>
        runDashboard(owner, repo, autoRefresh, interval, reposOpt)

      case Command.List(owner, repo, status, branch, limit, reposOpt) =>
        listWorkflowRuns(owner, repo, status, branch, limit, reposOpt)

      case Command.Show(owner, repo, runId, reposOpt) =>
        showWorkflowRun(owner, repo, runId, reposOpt)

      case Command.Rerun(owner, repo, runId, failedOnly, reposOpt) =>
        rerunWorkflow(owner, repo, runId, failedOnly, reposOpt)

      case Command.Cancel(owner, repo, runId, reposOpt) =>
        cancelWorkflow(owner, repo, runId, reposOpt)

      case Command.Init =>
        CliConfig.createSampleConfig[F]

      case Command.Version =>
        showVersion

  /** Run interactive dashboard */
  private def runDashboard(
      owner: String,
      repo: String,
      autoRefresh: Boolean,
      interval: Int,
      reposOpt: Option[scala.List[com.github.actions.domain.Repository]]
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
          repo,
          reposOpt
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
      limit: Int,
      reposOpt: Option[scala.List[com.github.actions.domain.Repository]]
  ): F[Unit] =
    EmberClientBuilder.default[F].build.use { client =>
      val filter = RunFilter(
        status = status,
        branch = branch,
        actor = None
      )

      for
        gitHubClient <- createGitHubClient(client)
        _ <- reposOpt match
          case Some(repos) if repos.nonEmpty =>
            val fetches = repos.traverse { r =>
              gitHubClient
                .listWorkflowRuns(r.owner, r.name, Some(filter))
                .map(_.map(run => (r, run)))
            }
            fetches.map(_.flatten)
              .map(_.sortBy(_._2.updatedAt)(Ordering[java.time.Instant].reverse))
              .map(_.take(limit))
              .flatMap(printWorkflowRunsMulti)
          case _ =>
            gitHubClient.listWorkflowRuns(owner, repo, Some(filter)).flatMap {
              runs =>
                val limitedRuns = runs.take(limit)
                printWorkflowRuns(limitedRuns)
            }
      yield ()
    }

  /** Show workflow run details */
  private def showWorkflowRun(
      owner: String,
      repo: String,
      runId: Long,
      reposOpt: Option[scala.List[com.github.actions.domain.Repository]]
  ): F[Unit] =
    EmberClientBuilder.default[F].build.use { client =>
      for
        gitHubClient <- createGitHubClient(client)
        _ <- reposOpt match
          case Some(repos) if repos.nonEmpty =>
            val fetches = repos.traverse { r =>
              for
                run <- gitHubClient.getWorkflowRun(r.owner, r.name, runId)
                jobs <- gitHubClient.listWorkflowRunJobs(r.owner, r.name, runId)
              yield (r, run, jobs)
            }
            fetches.flatMap(printWorkflowRunDetailMulti)
          case _ =>
            for
              run <- gitHubClient.getWorkflowRun(owner, repo, runId)
              jobs <- gitHubClient.listWorkflowRunJobs(owner, repo, runId)
              _ <- printWorkflowRunDetail(run, jobs)
            yield ()
      yield ()
    }

  /** Rerun workflow */
  private def rerunWorkflow(
      owner: String,
      repo: String,
      runId: Long,
      failedOnly: Boolean,
      reposOpt: Option[scala.List[com.github.actions.domain.Repository]]
  ): F[Unit] =
    EmberClientBuilder.default[F].build.use { client =>
      for
        gitHubClient <- createGitHubClient(client)
        _ <- reposOpt match
          case Some(repos) if repos.nonEmpty =>
            repos.traverse_ { r =>
              val act = if failedOnly then gitHubClient.rerunFailedJobs(r.owner, r.name, runId)
              else gitHubClient.rerunWorkflow(r.owner, r.name, runId)
              act *> Async[F].delay(println(s"${r.fullName}: rerun requested for $runId"))
            }
          case _ =>
            val act = if failedOnly then gitHubClient.rerunFailedJobs(owner, repo, runId)
            else gitHubClient.rerunWorkflow(owner, repo, runId)
            act *> Async[F].delay(println(s"Workflow run $runId rerun requested"))
      yield ()
    }

  /** Cancel workflow run */
  private def cancelWorkflow(
      owner: String,
      repo: String,
      runId: Long
      , reposOpt: Option[scala.List[com.github.actions.domain.Repository]]
  ): F[Unit] =
    EmberClientBuilder.default[F].build.use { client =>
      for
        gitHubClient <- createGitHubClient(client)
        _ <- reposOpt match
          case Some(repos) if repos.nonEmpty =>
            repos.traverse_ { r =>
              gitHubClient.cancelWorkflowRun(r.owner, r.name, runId) *>
                Async[F].delay(println(s"${r.fullName}: run $runId cancelled"))
            }
          case _ =>
            gitHubClient.cancelWorkflowRun(owner, repo, runId) *>
              Async[F].delay(println(s"Workflow run $runId cancelled"))
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

  private def printWorkflowRunsMulti(
      data: List[(com.github.actions.domain.Repository, com.github.actions.domain.WorkflowRun)]
  ): F[Unit] =
    Async[F].delay {
      println(s"Found ${data.length} workflow runs across ${data.map(_._1.fullName).distinct.length} repos:")
      data.foreach { case (repo, run) =>
        val status = run.conclusion.map(_.toString).getOrElse(run.status.toString)
        println(f"  ${repo.fullName}%-30s ${run.id}%10d  ${run.name}%-30s  $status")
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

  private def printWorkflowRunDetailMulti(
      data: scala.List[(com.github.actions.domain.Repository, com.github.actions.domain.WorkflowRun, scala.List[com.github.actions.domain.Job])]
  ): F[Unit] =
    Async[F].delay {
      data.foreach { case (repo, run, jobs) =>
        println(s"Repository: ${repo.fullName}")
        println(s"Workflow Run: ${run.name}")
        println(s"  ID: ${run.id}")
        println(s"  Status: ${run.status}")
        println(s"  Conclusion: ${run.conclusion.map(_.toString).getOrElse("N/A")}")
        println(s"  Branch: ${run.headBranch}")
        println(s"  Jobs: ${jobs.length}")
        jobs.foreach { job =>
          val status = job.conclusion.map(_.toString).getOrElse(job.status.toString)
          println(f"    - ${job.name}%-40s  $status")
        }
      }
    }

object CommandExecutor:
  def apply[F[_]: Async: Console](config: CliConfig): CommandExecutor[F] =
    new CommandExecutor[F](config)
