package com.github.actions.client

import cats.effect.kernel.Async
import cats.effect.kernel.Ref
import cats.syntax.all.*
import java.time.Instant
import com.github.actions.domain.{WorkflowRun, Job, AssistantSuggestion, AssistantAction, AssistantActionCommand, WorkflowConclusion}
import com.github.actions.service.{PromptBuilder, RedactionService}

case class CacheKey(owner: String, repo: String, runId: Long, jobId: Option[Long])

class AssistantService[F[_]: Async](
  gh: GitHubClient[F],
  model: ModelClient[F],
  config: ModelConfig,
  cache: Ref[F, Map[CacheKey, (Instant, List[AssistantSuggestion])]]
):
  private val ttlSeconds = 15 * 60

  def analyze(owner: String, repo: String, run: WorkflowRun, job: Option[Job]): F[List[AssistantSuggestion]] =
    val key = CacheKey(owner, repo, run.id, job.map(_.id))
    for
      now <- Async[F].delay(Instant.now())
      cached <- cache.get.map(_.get(key).filter { case (ts, _) => now.getEpochSecond - ts.getEpochSecond < ttlSeconds })
      result <- cached match
        case Some((_, suggestions)) => Async[F].pure(suggestions)
        case None =>
          for
            logsOpt <- job.traverse(j => gh.getJobLogs(owner, repo, j.id).attempt.map(_.toOption)).map(_.flatten)
            logLinesCount = sys.env.get("AI_ASSISTANT_LOG_LINES").flatMap(_.toIntOption).map(n => n.min(200).max(60)).getOrElse(120)
            lines = logsOpt.map(_.split("\n").toList).getOrElse(Nil)
            redacted = RedactionService.redactLines(lines).takeRight(logLinesCount)
            failingSteps = job.map(_.steps.filter(_.conclusion.contains(WorkflowConclusion.Failure)).map(_.name)).getOrElse(Nil)
            errorLine = redacted.reverse.find { l =>
              val x = l.toLowerCase
              x.contains("error") || x.contains("exception") || x.contains("failed") || x.contains("timeout") || x.contains("enoent") || x.contains("module not found") || x.contains("permission denied") || x.contains("outofmemory")
            }
            prompt = {
              val stepSummary = if failingSteps.nonEmpty then s"Failing steps: ${failingSteps.mkString(", ")}" else "Failing steps: (none reported)"
              val err = errorLine.getOrElse("(no clear error line found)")
              val header = s"Run: ${run.name} (${run.headBranch})\nJob: ${job.map(_.name).getOrElse("(none)")}\nStatus: ${run.status} Conclusion: ${run.conclusion.map(_.toString).getOrElse("N/A")}\n${stepSummary}\nLast error: ${err}\n"
              header + "\nRecent logs (tail):\n" + redacted.takeRight(50).mkString("\n")
            }
            raw <- model.complete(prompt, config).attempt
            rationale = raw.toOption match
              case Some(txt) if txt.nonEmpty => txt
              case _ =>
                val logMsg = if logsOpt.isEmpty then "No logs available." else "No clear error found."
                s"${logMsg} Run status: ${run.status.toString}, conclusion: ${run.conclusion.map(_.toString).getOrElse("N/A")}."
            suggestions = {
              val cmd = AssistantAction.Command(AssistantActionCommand("gh-actions rerun --failed-only", "Retry failed jobs"))
              val baseRefs = List("https://docs.github.com/actions")
              val heuristics = errorLine.map(_.toLowerCase).map { e =>
                if e.contains("timeout") then List(AssistantAction.Link(com.github.actions.domain.AssistantActionLink("https://docs.github.com/actions/monitoring-and-troubleshooting-workflows","Troubleshoot timeouts")))
                else if e.contains("enoent") || e.contains("no such file") then List(AssistantAction.Link(com.github.actions.domain.AssistantActionLink("https://docs.github.com/actions/using-workflows/workflow-syntax-for-github-actions#using-paths","Check file paths in workflow")))
                else if e.contains("module not found") then List(AssistantAction.Link(com.github.actions.domain.AssistantActionLink("https://docs.github.com/actions/using-workflows/workflow-syntax-for-github-actions#using-actions-in-a-workflow","Verify dependencies/modules")))
                else if e.contains("permission denied") then List(AssistantAction.Link(com.github.actions.domain.AssistantActionLink("https://docs.github.com/actions/security-guides/","Permissions troubleshooting")))
                else if e.contains("outofmemory") || e.contains("java heap space") then List(AssistantAction.Link(com.github.actions.domain.AssistantActionLink("https://docs.github.com/actions/using-github-hosted-runners/about-github-hosted-runners","Runner resources and memory")))
                else Nil
              }.getOrElse(Nil)
              val s = AssistantSuggestion(
                title = s"Review '${job.map(_.name).getOrElse(run.name)}'",
                rationale = rationale,
                actions = cmd :: heuristics,
                references = baseRefs,
                confidence = None
              )
              List(s)
            }
            _ <- cache.update(_ + (key -> (now -> suggestions)))
          yield suggestions
    yield result

object AssistantService:
  def create[F[_]: Async](gh: GitHubClient[F], model: ModelClient[F], config: ModelConfig): F[AssistantService[F]] =
    Ref.of[F, Map[CacheKey, (Instant, List[AssistantSuggestion])]](Map.empty).map { ref =>
      new AssistantService[F](gh, model, config, ref)
    }