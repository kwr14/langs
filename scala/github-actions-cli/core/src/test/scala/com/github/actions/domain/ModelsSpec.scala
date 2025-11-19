package com.github.actions.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.time.Instant

class ModelsSpec extends AnyFlatSpec with Matchers:

  "Repository.fromFullName" should "parse valid repository names" in {
    Repository.fromFullName("owner/repo") shouldBe Some(
      Repository("owner", "repo")
    )
  }

  it should "return None for invalid names" in {
    Repository.fromFullName("invalid") shouldBe None
    Repository.fromFullName("owner/repo/extra") shouldBe None
    Repository.fromFullName("") shouldBe None
  }

  "Repository.fullName" should "return owner/name format" in {
    Repository("owner", "repo").fullName shouldBe "owner/repo"
  }

  "Job.duration" should "calculate duration when both timestamps present" in {
    val start = Instant.parse("2024-01-01T10:00:00Z")
    val end = Instant.parse("2024-01-01T10:05:00Z")
    val job = Job(
      id = 1,
      name = "test",
      status = WorkflowStatus.Completed,
      conclusion = Some(WorkflowConclusion.Success),
      startedAt = Some(start),
      completedAt = Some(end),
      steps = List.empty
    )
    job.duration shouldBe Some(300) // 5 minutes = 300 seconds
  }

  it should "return None when timestamps missing" in {
    val job = Job(
      id = 1,
      name = "test",
      status = WorkflowStatus.Queued,
      conclusion = None,
      startedAt = None,
      completedAt = None,
      steps = List.empty
    )
    job.duration shouldBe None
  }

  "Job status helpers" should "correctly identify job states" in {
    val runningJob = Job(
      id = 1,
      name = "test",
      status = WorkflowStatus.InProgress,
      conclusion = None,
      startedAt = Some(Instant.now()),
      completedAt = None,
      steps = List.empty
    )
    runningJob.isRunning shouldBe true
    runningJob.isCompleted shouldBe false

    val completedJob = runningJob.copy(
      status = WorkflowStatus.Completed,
      conclusion = Some(WorkflowConclusion.Success),
      completedAt = Some(Instant.now())
    )
    completedJob.isRunning shouldBe false
    completedJob.isCompleted shouldBe true
    completedJob.isSuccessful shouldBe true
    completedJob.isFailed shouldBe false

    val failedJob =
      completedJob.copy(conclusion = Some(WorkflowConclusion.Failure))
    failedJob.isSuccessful shouldBe false
    failedJob.isFailed shouldBe true
  }

  "WorkflowRun.runningJobs" should "filter running jobs" in {
    val runningJob = Job(
      id = 1,
      name = "running",
      status = WorkflowStatus.InProgress,
      conclusion = None,
      startedAt = Some(Instant.now()),
      completedAt = None,
      steps = List.empty
    )
    val completedJob = Job(
      id = 2,
      name = "completed",
      status = WorkflowStatus.Completed,
      conclusion = Some(WorkflowConclusion.Success),
      startedAt = Some(Instant.now()),
      completedAt = Some(Instant.now()),
      steps = List.empty
    )
    val run = WorkflowRun(
      id = 1,
      name = "test",
      status = WorkflowStatus.InProgress,
      conclusion = None,
      headBranch = "main",
      headSha = "abc123",
      actor = Actor("user", "https://example.com/avatar.png"),
      createdAt = Instant.now(),
      updatedAt = Instant.now(),
      runStartedAt = Some(Instant.now()),
      htmlUrl = "https://github.com/owner/repo/actions/runs/1",
      jobs = List(runningJob, completedJob)
    )
    run.runningJobs should have length 1
    run.runningJobs.head.name shouldBe "running"
  }

  "RunFilter.matches" should "filter by status" in {
    val filter = RunFilter(status = Some(WorkflowStatus.Completed))
    val run = WorkflowRun(
      id = 1,
      name = "test",
      status = WorkflowStatus.Completed,
      conclusion = Some(WorkflowConclusion.Success),
      headBranch = "main",
      headSha = "abc123",
      actor = Actor("user", "https://example.com/avatar.png"),
      createdAt = Instant.now(),
      updatedAt = Instant.now(),
      runStartedAt = Some(Instant.now()),
      htmlUrl = "https://github.com/owner/repo/actions/runs/1",
      jobs = List.empty
    )
    filter.matches(run) shouldBe true

    val runningRun =
      run.copy(status = WorkflowStatus.InProgress, conclusion = None)
    filter.matches(runningRun) shouldBe false
  }

  it should "filter by branch" in {
    val filter = RunFilter(branch = Some("main"))
    val run = WorkflowRun(
      id = 1,
      name = "test",
      status = WorkflowStatus.Completed,
      conclusion = Some(WorkflowConclusion.Success),
      headBranch = "main",
      headSha = "abc123",
      actor = Actor("user", "https://example.com/avatar.png"),
      createdAt = Instant.now(),
      updatedAt = Instant.now(),
      runStartedAt = Some(Instant.now()),
      htmlUrl = "https://github.com/owner/repo/actions/runs/1",
      jobs = List.empty
    )
    filter.matches(run) shouldBe true

    val featureRun = run.copy(headBranch = "feature")
    filter.matches(featureRun) shouldBe false
  }

  it should "filter by actor" in {
    val filter = RunFilter(actor = Some("user"))
    val run = WorkflowRun(
      id = 1,
      name = "test",
      status = WorkflowStatus.Completed,
      conclusion = Some(WorkflowConclusion.Success),
      headBranch = "main",
      headSha = "abc123",
      actor = Actor("user", "https://example.com/avatar.png"),
      createdAt = Instant.now(),
      updatedAt = Instant.now(),
      runStartedAt = Some(Instant.now()),
      htmlUrl = "https://github.com/owner/repo/actions/runs/1",
      jobs = List.empty
    )
    filter.matches(run) shouldBe true

    val otherRun =
      run.copy(actor = Actor("other", "https://example.com/avatar.png"))
    filter.matches(otherRun) shouldBe false
  }
