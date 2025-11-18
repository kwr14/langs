package com.github.actions.ui

import cats.effect.kernel.{Async, Ref}
import cats.effect.std.Queue
import cats.syntax.all.*
import cats.effect.syntax.spawn.*
import fs2.Stream
import com.github.actions.client.GitHubClient
import com.github.actions.domain.{Repository, WorkflowRun, Job}
import scala.concurrent.duration.*

/** Dashboard component that manages the interactive UI */
class Dashboard[F[_]: Async](
    client: GitHubClient[F],
    terminal: Terminal[F],
    keyReader: KeyReader[F],
    stateRef: Ref[F, DashboardState]
):

  /** Render the current state to the terminal */
  def render: F[Unit] =
    for
      state <- stateRef.get
      (width, height) <- terminal.size
      _ <- terminal.clear
      _ <- terminal.moveCursor(0, 0)
      _ <- renderView(state, width, height)
      _ <- terminal.flush
    yield ()

  /** Render the appropriate view based on state */
  private def renderView(
      state: DashboardState,
      width: Int,
      height: Int
  ): F[Unit] =
    state.viewMode match
      case ViewMode.RunList =>
        renderRunList(state, width, height)
      case ViewMode.RunDetail =>
        renderRunDetail(state, width, height)
      case ViewMode.JobDetail =>
        renderJobDetail(state, width, height)
      case ViewMode.Help =>
        renderHelp(width, height)

  /** Render the run list view */
  private def renderRunList(
      state: DashboardState,
      width: Int,
      height: Int
  ): F[Unit] =
    val bounds = Rect(0, 0, width, height)
    val layout = Layout.vertical(
      bounds,
      List(
        Constraint.Length(3), // Header
        Constraint.Min(10), // Run list
        Constraint.Length(2) // Footer
      )
    )

    for
      _ <- renderHeader(state, layout(0))
      _ <- renderRuns(state, layout(1))
      _ <- renderFooter(state, layout(2))
    yield ()

  /** Render the run detail view */
  private def renderRunDetail(
      state: DashboardState,
      width: Int,
      height: Int
  ): F[Unit] =
    state.selectedRun match
      case Some(run) =>
        val bounds = Rect(0, 0, width, height)
        val layout = Layout.vertical(
          bounds,
          List(
            Constraint.Length(5), // Run header
            Constraint.Min(10), // Job list
            Constraint.Length(2) // Footer
          )
        )

        for
          _ <- renderRunHeader(run, layout(0))
          _ <- renderJobs(state, run, layout(1))
          _ <- renderFooter(state, layout(2))
        yield ()
      case None =>
        terminal.writeLine("No run selected")

  /** Render the job detail view */
  private def renderJobDetail(
      state: DashboardState,
      width: Int,
      height: Int
  ): F[Unit] =
    (state.selectedRun, state.selectedJob) match
      case (Some(run), Some(job)) =>
        val bounds = Rect(0, 0, width, height)
        val layout = Layout.vertical(
          bounds,
          List(
            Constraint.Length(3), // Job header
            Constraint.Min(10), // Step list
            Constraint.Length(2) // Footer
          )
        )

        for
          _ <- renderJobHeader(job, layout(0))
          _ <- renderSteps(job, layout(1))
          _ <- renderFooter(state, layout(2))
        yield ()
      case _ =>
        terminal.writeLine("No job selected")

  /** Render help screen */
  private def renderHelp(width: Int, height: Int): F[Unit] =
    val helpText = List(
      Style.title("GitHub Actions Dashboard - Help").render,
      "",
      Style.subtitle("Navigation:").render,
      "  ↑/k         Move up",
      "  ↓/j         Move down",
      "  Enter/Space Select item / Drill down",
      "  Esc/q       Go back",
      "  g           Go to top",
      "  G           Go to bottom",
      "  PgUp/PgDn   Page up/down",
      "",
      Style.subtitle("Actions:").render,
      "  r/F5        Refresh",
      "  f//         Filter",
      "  ?/F1        Toggle help",
      "  Ctrl+C/D    Quit",
      "",
      "Press any key to close help..."
    )

    helpText.traverse_(line => terminal.writeLine(line))

  /** Render header */
  private def renderHeader(state: DashboardState, bounds: Rect): F[Unit] =
    val title = Style.title(s"${state.owner}/${state.repo} - Workflow Runs")
    val subtitle = state.lastRefresh match
      case Some(time) =>
        val relTime = WorkflowComponents.formatRelativeTime(time)
        Style.dim(s"Last refresh: $relTime")
      case None =>
        Style.dim("Not refreshed yet")

    for
      _ <- terminal.writeLine(title.render)
      _ <- terminal.writeLine(subtitle.render)
      _ <- terminal.writeLine(Style.horizontalLine(bounds.width).render)
    yield ()

  /** Render footer */
  private def renderFooter(state: DashboardState, bounds: Rect): F[Unit] =
    val statusLine =
      if state.isLoading then Style.info("Loading...")
      else
        state.error match
          case Some(err) => Style.error(s"Error: $err")
          case None      => Style.dim("Press ? for help")

    for
      _ <- terminal.writeLine(Style.horizontalLine(bounds.width).render)
      _ <- terminal.writeLine(statusLine.render)
    yield ()

  /** Render run header */
  private def renderRunHeader(run: WorkflowRun, bounds: Rect): F[Unit] =
    val statusBadge = run.conclusion match
      case Some(conclusion) => Style.conclusionBadge(conclusion)
      case None             => Style.statusBadge(run.status)

    val title = Style.title(run.name)
    val subtitle = Style.dim(s"#${run.id} • ${run.headBranch}")
    val time = Style.dim(WorkflowComponents.formatRelativeTime(run.createdAt))

    for
      _ <- terminal.writeLine((statusBadge ++ fansi.Str(" ") ++ title).render)
      _ <- terminal.writeLine(subtitle.render)
      _ <- terminal.writeLine(time.render)
      _ <- terminal.writeLine(Style.horizontalLine(bounds.width).render)
      _ <- terminal.writeLine("")
    yield ()

  /** Render job header */
  private def renderJobHeader(job: Job, bounds: Rect): F[Unit] =
    val statusBadge = job.conclusion match
      case Some(conclusion) => Style.conclusionBadge(conclusion)
      case None             => Style.statusBadge(job.status)

    val title = Style.title(job.name)

    for
      _ <- terminal.writeLine((statusBadge ++ fansi.Str(" ") ++ title).render)
      _ <- terminal.writeLine(Style.horizontalLine(bounds.width).render)
      _ <- terminal.writeLine("")
    yield ()

  /** Render runs list */
  private def renderRuns(state: DashboardState, bounds: Rect): F[Unit] =
    val runs = state.filteredRuns
    val visibleRuns = runs.zipWithIndex.slice(
      state.scrollOffset,
      state.scrollOffset + bounds.height
    )

    visibleRuns.traverse_ { case (run, idx) =>
      val isSelected = idx == state.selectedRunIndex
      val line = WorkflowComponents.renderWorkflowRunLine(run, isSelected)
      terminal.writeLine(line.render)
    }

  /** Render jobs list */
  private def renderJobs(
      state: DashboardState,
      run: WorkflowRun,
      bounds: Rect
  ): F[Unit] =
    run.jobs.zipWithIndex.traverse_ { case (job, idx) =>
      val isSelected = idx == state.selectedJobIndex
      val line = WorkflowComponents.renderJobLine(job, isSelected)
      terminal.writeLine(line.render)
    }

  /** Render steps list */
  private def renderSteps(job: Job, bounds: Rect): F[Unit] =
    job.steps.traverse_ { step =>
      val line = WorkflowComponents.renderStepLine(step, indent = 0)
      terminal.writeLine(line.render)
    }

  /** Handle a navigation action */
  def handleAction(action: NavigationAction): F[Boolean] =
    action match
      case NavigationAction.MoveUp =>
        stateRef.update(_.moveUp) >> render >> Async[F].pure(true)
      case NavigationAction.MoveDown =>
        stateRef.update(_.moveDown) >> render >> Async[F].pure(true)
      case NavigationAction.PageUp =>
        stateRef.update(_.pageUp(10)) >> render >> Async[F].pure(true)
      case NavigationAction.PageDown =>
        stateRef.update(_.pageDown(10)) >> render >> Async[F].pure(true)
      case NavigationAction.GoToTop =>
        stateRef.update(_.goToTop) >> render >> Async[F].pure(true)
      case NavigationAction.GoToBottom =>
        stateRef.update(_.goToBottom) >> render >> Async[F].pure(true)
      case NavigationAction.Select =>
        stateRef.update(_.select) >> render >> Async[F].pure(true)
      case NavigationAction.Back =>
        stateRef.update(_.back) >> render >> Async[F].pure(true)
      case NavigationAction.Help =>
        stateRef.update(_.toggleHelp) >> render >> Async[F].pure(true)
      case NavigationAction.Refresh =>
        refresh >> Async[F].pure(true)
      case NavigationAction.Quit =>
        Async[F].pure(false) // Signal to exit
      case _ =>
        Async[F].pure(true)

  /** Refresh workflow runs from GitHub */
  def refresh: F[Unit] =
    for
      state <- stateRef.get
      _ <- stateRef.update(_.setLoading(true))
      _ <- render
      result <- client
        .listWorkflowRuns(
          state.owner,
          state.repo,
          state.filter
        )
        .attempt
      _ <- result match
        case Right(runs) =>
          stateRef.update(_.updateRuns(runs))
        case Left(err) =>
          stateRef.update(_.setError(err.getMessage))
      _ <- render
    yield ()

  /** Event loop - process keyboard events */
  def eventLoop: F[Unit] =
    Stream
      .repeatEval(keyReader.readKey)
      .map(NavigationAction.fromKeyEvent)
      .evalMap(handleAction)
      .takeWhile(identity) // Continue while handleAction returns true
      .compile
      .drain

  /** Auto-refresh stream */
  def autoRefresh(interval: FiniteDuration): Stream[F, Unit] =
    Stream
      .awakeEvery[F](interval)
      .evalMap(_ => refresh)

  /** Run the dashboard */
  def run(
      autoRefreshInterval: Option[FiniteDuration] = Some(30.seconds)
  ): F[Unit] =
    for
      _ <- terminal.clear
      _ <- terminal.hideCursor
      _ <- refresh // Initial load
      _ <- render
      _ <- autoRefreshInterval match
        case Some(interval) =>
          // Run event loop and auto-refresh concurrently
          eventLoop.both(autoRefresh(interval).compile.drain).void
        case None =>
          // Just run event loop
          eventLoop
      _ <- terminal.showCursor
      _ <- terminal.clear
    yield ()

object Dashboard:
  import cats.effect.kernel.Async

  /** Create a new dashboard */
  def apply[F[_]: Async](
      client: GitHubClient[F],
      terminal: Terminal[F],
      keyReader: KeyReader[F],
      owner: String,
      repo: String
  ): F[Dashboard[F]] =
    for stateRef <- Ref.of[F, DashboardState](
        DashboardState(owner = owner, repo = repo)
      )
    yield new Dashboard[F](client, terminal, keyReader, stateRef)
