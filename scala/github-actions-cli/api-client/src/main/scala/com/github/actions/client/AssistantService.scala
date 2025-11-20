package com.github.actions.client

import cats.effect.kernel.Async
import cats.effect.kernel.Ref
import cats.syntax.all.*
import java.time.Instant
import com.github.actions.domain.{WorkflowRun, Job, AssistantSuggestion, AssistantAction, AssistantActionCommand}
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
            lines = logsOpt.map(_.split("\n").toList).getOrElse(Nil)
            redacted = RedactionService.redactLines(lines).takeRight(200)
            prompt = PromptBuilder.build(run, job, redacted)
            raw <- model.complete(prompt, config).attempt
            rationale = raw.toOption match
              case Some(txt) if txt.nonEmpty => txt
              case _ =>
                val logMsg = if logsOpt.isEmpty then "No logs available." else "No clear error found."
                s"${logMsg} Run status: ${run.status.toString}, conclusion: ${run.conclusion.map(_.toString).getOrElse("N/A")}."
            suggestion = AssistantSuggestion(
              title = s"Review '${job.map(_.name).getOrElse(run.name)}'",
              rationale = rationale,
              actions = List(AssistantAction.Command(AssistantActionCommand("gh-actions rerun --failed-only", "Retry failed jobs"))),
              references = List("https://docs.github.com/actions"),
              confidence = None
            )
            suggestions = List(suggestion)
            _ <- cache.update(_ + (key -> (now -> suggestions)))
          yield suggestions
    yield result

object AssistantService:
  def create[F[_]: Async](gh: GitHubClient[F], model: ModelClient[F], config: ModelConfig): F[AssistantService[F]] =
    Ref.of[F, Map[CacheKey, (Instant, List[AssistantSuggestion])]](Map.empty).map { ref =>
      new AssistantService[F](gh, model, config, ref)
    }