package com.github.actions.domain

import java.time.Instant
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import io.circe.syntax._

/** Represents a GitHub user or bot */
case class Actor(
    login: String,
    avatarUrl: String
)

object Actor:
  given Decoder[Actor] = Decoder.forProduct2("login", "avatar_url")(Actor.apply)
  given Encoder[Actor] =
    Encoder.forProduct2("login", "avatar_url")(a => (a.login, a.avatarUrl))

/** Represents a GitHub repository */
case class Repository(
    owner: String,
    name: String
):
  def fullName: String = s"$owner/$name"

object Repository:
  given Decoder[Repository] = deriveDecoder
  given Encoder[Repository] = deriveEncoder

  def fromFullName(fullName: String): Option[Repository] =
    fullName.split("/") match
      case Array(owner, name) => Some(Repository(owner, name))
      case _                  => None

/** Represents a step within a job */
case class Step(
    name: String,
    status: WorkflowStatus,
    conclusion: Option[WorkflowConclusion],
    number: Int,
    startedAt: Option[Instant],
    completedAt: Option[Instant]
)

object Step:
  given Decoder[Step] = Decoder.forProduct6(
    "name",
    "status",
    "conclusion",
    "number",
    "started_at",
    "completed_at"
  )(Step.apply)
  given Encoder[Step] = Encoder.forProduct6(
    "name",
    "status",
    "conclusion",
    "number",
    "started_at",
    "completed_at"
  )(s => (s.name, s.status, s.conclusion, s.number, s.startedAt, s.completedAt))

/** Represents a job within a workflow run */
case class Job(
    id: Long,
    name: String,
    status: WorkflowStatus,
    conclusion: Option[WorkflowConclusion],
    startedAt: Option[Instant],
    completedAt: Option[Instant],
    steps: List[Step],
    logs: Option[String] = None // Job logs (fetched separately)
):
  def duration: Option[Long] =
    for
      start <- startedAt
      end <- completedAt
    yield java.time.Duration.between(start, end).getSeconds

  def isRunning: Boolean = status == WorkflowStatus.InProgress
  def isCompleted: Boolean = status == WorkflowStatus.Completed
  def isSuccessful: Boolean = conclusion.contains(WorkflowConclusion.Success)
  def isFailed: Boolean = conclusion.contains(WorkflowConclusion.Failure)

object Job:
  given Decoder[Job] = Decoder.forProduct7(
    "id",
    "name",
    "status",
    "conclusion",
    "started_at",
    "completed_at",
    "steps"
  )((id, name, status, conclusion, startedAt, completedAt, steps) =>
    Job(id, name, status, conclusion, startedAt, completedAt, steps, None)
  )
  given Encoder[Job] = Encoder.forProduct7(
    "id",
    "name",
    "status",
    "conclusion",
    "started_at",
    "completed_at",
    "steps"
  )(j =>
    (j.id, j.name, j.status, j.conclusion, j.startedAt, j.completedAt, j.steps)
  )

/** Represents a workflow run */
case class WorkflowRun(
    id: Long,
    name: String,
    status: WorkflowStatus,
    conclusion: Option[WorkflowConclusion],
    headBranch: String,
    headSha: String,
    actor: Actor,
    createdAt: Instant,
    updatedAt: Instant,
    runStartedAt: Option[Instant],
    htmlUrl: String,
    jobs: List[Job]
):
  def duration: Option[Long] =
    for
      start <- runStartedAt
      end <-
        if status == WorkflowStatus.Completed then Some(updatedAt) else None
    yield java.time.Duration.between(start, end).getSeconds

  def isRunning: Boolean = status == WorkflowStatus.InProgress
  def isCompleted: Boolean = status == WorkflowStatus.Completed
  def isSuccessful: Boolean = conclusion.contains(WorkflowConclusion.Success)
  def isFailed: Boolean = conclusion.contains(WorkflowConclusion.Failure)

  def runningJobs: List[Job] = jobs.filter(_.isRunning)
  def completedJobs: List[Job] = jobs.filter(_.isCompleted)
  def failedJobs: List[Job] = jobs.filter(_.isFailed)

object WorkflowRun:
  given Decoder[WorkflowRun] = Decoder.instance { c =>
    for
      id <- c.get[Long]("id")
      name <- c.get[String]("name")
      status <- c.get[WorkflowStatus]("status")
      conclusion <- c.get[Option[WorkflowConclusion]]("conclusion")
      headBranch <- c.get[String]("head_branch")
      headSha <- c.get[String]("head_sha")
      actor <- c.get[Actor]("actor")
      createdAt <- c.get[Instant]("created_at")
      updatedAt <- c.get[Instant]("updated_at")
      runStartedAt <- c.get[Option[Instant]]("run_started_at")
      htmlUrl <- c.get[String]("html_url")
      jobs <- c.getOrElse[List[Job]]("jobs")(List.empty)
    yield WorkflowRun(
      id,
      name,
      status,
      conclusion,
      headBranch,
      headSha,
      actor,
      createdAt,
      updatedAt,
      runStartedAt,
      htmlUrl,
      jobs
    )
  }

  given Encoder[WorkflowRun] = Encoder.instance { run =>
    io.circe.Json.obj(
      "id" -> run.id.asJson,
      "name" -> run.name.asJson,
      "status" -> run.status.asJson,
      "conclusion" -> run.conclusion.asJson,
      "head_branch" -> run.headBranch.asJson,
      "head_sha" -> run.headSha.asJson,
      "actor" -> run.actor.asJson,
      "created_at" -> run.createdAt.asJson,
      "updated_at" -> run.updatedAt.asJson,
      "run_started_at" -> run.runStartedAt.asJson,
      "jobs" -> run.jobs.asJson
    )
  }

/** Represents a filter for workflow runs */
case class RunFilter(
    status: Option[WorkflowStatus] = None,
    branch: Option[String] = None,
    actor: Option[String] = None
):
  def matches(run: WorkflowRun): Boolean =
    status.forall(_ == run.status) &&
      branch.forall(_ == run.headBranch) &&
      actor.forall(_ == run.actor.login)

object RunFilter:
  val empty: RunFilter = RunFilter()

  given Decoder[RunFilter] = deriveDecoder
  given Encoder[RunFilter] = deriveEncoder

/** Represents the state of the dashboard */
case class DashboardState(
    runs: List[WorkflowRun],
    selectedIndex: Int,
    filter: RunFilter,
    lastUpdate: Instant,
    error: Option[String]
):
  def selectedRun: Option[WorkflowRun] =
    if selectedIndex >= 0 && selectedIndex < runs.length then
      Some(runs(selectedIndex))
    else None

  def filteredRuns: List[WorkflowRun] =
    runs.filter(filter.matches)

object DashboardState:
  def empty: DashboardState = DashboardState(
    runs = List.empty,
    selectedIndex = 0,
    filter = RunFilter.empty,
    lastUpdate = Instant.now(),
    error = None
  )

  given Decoder[DashboardState] = deriveDecoder
  given Encoder[DashboardState] = deriveEncoder
