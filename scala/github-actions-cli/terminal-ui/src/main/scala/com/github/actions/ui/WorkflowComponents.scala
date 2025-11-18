package com.github.actions.ui

import cats.effect.kernel.Async
import cats.syntax.all.*
import fansi.Str
import com.github.actions.domain.{WorkflowRun, Job, Step, WorkflowStatus, WorkflowConclusion}
import java.time.Instant
import java.time.Duration as JDuration

/** Components for rendering workflow-related UI elements */
object WorkflowComponents:
  
  /** Format duration in human-readable format */
  def formatDuration(duration: JDuration): String =
    val seconds = duration.getSeconds
    if seconds < 60 then
      s"${seconds}s"
    else if seconds < 3600 then
      val minutes = seconds / 60
      val secs = seconds % 60
      s"${minutes}m ${secs}s"
    else
      val hours = seconds / 3600
      val minutes = (seconds % 3600) / 60
      s"${hours}h ${minutes}m"
  
  /** Format timestamp as relative time */
  def formatRelativeTime(instant: Instant): String =
    val now = Instant.now()
    val duration = JDuration.between(instant, now)
    val seconds = duration.getSeconds
    
    if seconds < 60 then
      s"${seconds}s ago"
    else if seconds < 3600 then
      val minutes = seconds / 60
      s"${minutes}m ago"
    else if seconds < 86400 then
      val hours = seconds / 3600
      s"${hours}h ago"
    else
      val days = seconds / 86400
      s"${days}d ago"
  
  /** Render a workflow run as a single line */
  def renderWorkflowRunLine(run: WorkflowRun, isSelected: Boolean): Str =
    val statusBadge = run.conclusion match
      case Some(conclusion) => Style.conclusionBadge(conclusion)
      case None => Style.statusBadge(run.status)
    
    val name = if isSelected then
      Style.highlight(run.name)
    else
      Str(run.name)
    
    val branch = Style.subtitle(s"[${run.headBranch}]")
    val actor = Style.subtitle(s"by ${run.actor.login}")
    val time = Style.subtitle(formatRelativeTime(run.createdAt))
    
    statusBadge ++ Str(" ") ++ name ++ Str(" ") ++ branch ++ Str(" ") ++ actor ++ Str(" ") ++ time
  
  /** Workflow run list component */
  class WorkflowRunList[F[_]: Async](
    runs: List[WorkflowRun],
    selectedIndex: Int = 0,
    offset: Int = 0
  ) extends Component[F, Unit]:
    override def render(state: Unit, bounds: Rect): F[RenderTree] =
      val visibleRuns = runs.drop(offset).take(bounds.height)
      val lines = visibleRuns.zipWithIndex.map { case (run, idx) =>
        val isSelected = (idx + offset) == selectedIndex
        renderWorkflowRunLine(run, isSelected)
      }
      Async[F].pure(RenderTree(lines))
  
  /** Render a job as a single line */
  def renderJobLine(job: Job, isSelected: Boolean): Str =
    val statusBadge = job.conclusion match
      case Some(conclusion) => Style.conclusionBadge(conclusion)
      case None => Style.statusBadge(job.status)
    
    val name = if isSelected then
      Style.highlight(job.name)
    else
      Str(job.name)
    
    val duration = (job.startedAt, job.completedAt) match
      case (Some(start), Some(end)) =>
        val d = JDuration.between(start, end)
        Style.subtitle(s"(${formatDuration(d)})")
      case (Some(start), None) =>
        val d = JDuration.between(start, Instant.now())
        Style.subtitle(s"(${formatDuration(d)}...)")
      case _ =>
        Str("")
    
    statusBadge ++ Str(" ") ++ name ++ Str(" ") ++ duration
  
  /** Job list component */
  class JobList[F[_]: Async](
    jobs: List[Job],
    selectedIndex: Int = 0
  ) extends Component[F, Unit]:
    override def render(state: Unit, bounds: Rect): F[RenderTree] =
      val lines = jobs.zipWithIndex.map { case (job, idx) =>
        val isSelected = idx == selectedIndex
        renderJobLine(job, isSelected)
      }
      Async[F].pure(RenderTree(lines))
  
  /** Render a step as a single line */
  def renderStepLine(step: Step, indent: Int = 0): Str =
    val statusBadge = step.conclusion match
      case Some(conclusion) => Style.conclusionBadge(conclusion)
      case None => Style.statusBadge(step.status)
    
    val indentation = Str(" " * indent)
    val name = Str(step.name)
    
    val duration = (step.startedAt, step.completedAt) match
      case (Some(start), Some(end)) =>
        val d = JDuration.between(start, end)
        Style.subtitle(s"(${formatDuration(d)})")
      case (Some(start), None) =>
        val d = JDuration.between(start, Instant.now())
        Style.subtitle(s"(${formatDuration(d)}...)")
      case _ =>
        Str("")
    
    indentation ++ statusBadge ++ Str(" ") ++ name ++ Str(" ") ++ duration
  
  /** Step list component */
  class StepList[F[_]: Async](
    steps: List[Step],
    indent: Int = 2
  ) extends Component[F, Unit]:
    override def render(state: Unit, bounds: Rect): F[RenderTree] =
      val lines = steps.map { step =>
        renderStepLine(step, indent)
      }
      Async[F].pure(RenderTree(lines))
  
  /** Job detail component showing steps */
  class JobDetail[F[_]: Async](job: Job) extends Component[F, Unit]:
    override def render(state: Unit, bounds: Rect): F[RenderTree] =
      val header = Style.title(s"Job: ${job.name}")
      val statusLine = Str("Status: ") ++ (job.conclusion match
        case Some(conclusion) => Style.conclusionBadge(conclusion)
        case None => Style.statusBadge(job.status)
      )
      
      val durationLine = (job.startedAt, job.completedAt) match
        case (Some(start), Some(end)) =>
          val d = JDuration.between(start, end)
          Str(s"Duration: ${formatDuration(d)}")
        case (Some(start), None) =>
          val d = JDuration.between(start, Instant.now())
          Str(s"Duration: ${formatDuration(d)} (running)")
        case _ =>
          Str("Duration: N/A")
      
      val stepsHeader = Style.subtitle("\nSteps:")
      val stepLines = job.steps.map(step => renderStepLine(step, 2))
      
      val allLines = List(header, statusLine, durationLine, stepsHeader) ++ stepLines
      Async[F].pure(RenderTree(allLines))

