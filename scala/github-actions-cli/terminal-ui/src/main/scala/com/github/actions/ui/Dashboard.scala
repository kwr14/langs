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
      "  o/O         Open run in browser",
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

  /** Render steps list and logs */
  private def renderSteps(job: Job, bounds: Rect): F[Unit] =
    for
      // Render steps summary
      _ <- job.steps.traverse_ { step =>
        val line = WorkflowComponents.renderStepLine(step, indent = 0)
        terminal.writeLine(line.render)
      }
      // Render logs if available
      _ <- job.logs match
        case Some(logs) if logs.nonEmpty =>
          for
            _ <- terminal.writeLine("")
            _ <- terminal.writeLine(Style.title("Job Logs:").render)
            _ <- terminal.writeLine(Style.horizontalLine(80).render)
            // Display last 50 lines of logs to avoid overwhelming the terminal
            _ <- logs.split("\n").toList.takeRight(50).traverse_ { line =>
              terminal.writeLine(line)
            }
          yield ()
        case _ =>
          for
            _ <- terminal.writeLine("")
            _ <- terminal.writeLine(Style.dim("(No logs available)").render)
          yield ()
    yield ()

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
        handleSelect >> Async[F].pure(true)
      case NavigationAction.Back =>
        stateRef.update(_.back) >> render >> Async[F].pure(true)
      case NavigationAction.Help =>
        stateRef.update(_.toggleHelp) >> render >> Async[F].pure(true)
      case NavigationAction.Refresh =>
        refresh >> Async[F].pure(true)
      case NavigationAction.OpenInBrowser =>
        openInBrowser >> Async[F].pure(true)
      case NavigationAction.Quit =>
        Async[F].pure(false) // Signal to exit
      case _ =>
        Async[F].pure(true)

  /** Handle select action based on current view mode */
  def handleSelect: F[Unit] =
    for
      state <- stateRef.get
      _ <- state.viewMode match
        case ViewMode.RunList =>
          // Load jobs for selected run and switch to RunDetail view
          selectCurrentRun
        case ViewMode.RunDetail =>
          // Load logs for selected job and switch to JobDetail view
          selectCurrentJob
        case _ =>
          Async[F].unit
    yield ()

  /** Select current job and load its logs */
  def selectCurrentJob: F[Unit] =
    for
      state <- stateRef.get
      _ <- state.selectedRun.flatMap(_.jobs.lift(state.selectedJobIndex)) match
        case Some(job) =>
          for
            _ <- stateRef.update(_.setLoading(true))
            _ <- render
            logs <- client.getJobLogs(state.owner, state.repo, job.id).attempt
            _ <- logs match
              case Right(logContent) =>
                // Update the job with logs
                val updatedJob = job.copy(logs = Some(logContent))
                val updatedRun = state.selectedRun.get.copy(
                  jobs = state.selectedRun.get.jobs
                    .updated(state.selectedJobIndex, updatedJob)
                )
                val updatedRuns = state.runs.map(r =>
                  if r.id == updatedRun.id then updatedRun else r
                )
                stateRef.update(s =>
                  s.copy(runs = updatedRuns).select.setLoading(false)
                )
              case Left(err) =>
                stateRef.update(
                  _.setError(s"Failed to load logs: ${err.getMessage}")
                )
            _ <- render
          yield ()
        case None =>
          Async[F].unit
    yield ()

  /** Open current workflow run in browser */
  def openInBrowser: F[Unit] =
    stateRef.get.flatMap { state =>
      state.selectedRun match
        case Some(run) =>
          // Use the 'open' command on macOS, 'xdg-open' on Linux, 'start' on Windows
          val openCommand = System.getProperty("os.name").toLowerCase match
            case os if os.contains("mac")                       => "open"
            case os if os.contains("nix") || os.contains("nux") => "xdg-open"
            case os if os.contains("win") => "cmd /c start"
            case _                        => "xdg-open" // Default to xdg-open

          Async[F].blocking {
            import scala.sys.process.*
            try s"$openCommand ${run.htmlUrl}".!
            catch case _: Exception => () // Silently ignore errors
          }.void
        case None =>
          Async[F].unit
    }

  /** Select current run and load its jobs */
  def selectCurrentRun: F[Unit] =
    for
      state <- stateRef.get
      _ <- state.selectedRun match
        case Some(run) =>
          for
            _ <- stateRef.update(_.setLoading(true))
            _ <- render
            jobs <- client
              .listWorkflowRunJobs(state.owner, state.repo, run.id)
              .attempt
            _ <- jobs match
              case Right(jobList) =>
                // Update the run with jobs
                val updatedRun = run.copy(jobs = jobList)
                val updatedRuns =
                  state.runs.map(r => if r.id == run.id then updatedRun else r)
                stateRef.update(s =>
                  s.copy(runs = updatedRuns).select.setLoading(false)
                )
              case Left(err) =>
                stateRef.update(
                  _.setError(s"Failed to load jobs: ${err.getMessage}")
                )
            _ <- render
          yield ()
        case None =>
          Async[F].unit
    yield ()

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
    // Use bracket to ensure cleanup happens even on error
    Async[F].bracket(
      // Acquire: setup terminal
      for
        _ <- terminal.enableRawMode
        _ <- terminal.enterAlternateScreen
        _ <- terminal.clear
        _ <- terminal.hideCursor
        _ <- refresh // Initial load
        _ <- render
      yield ()
    )(
      // Use: run the dashboard
      _ =>
        autoRefreshInterval match
          case Some(interval) =>
            // Run event loop and auto-refresh concurrently
            // Use race so that when eventLoop exits, we cancel autoRefresh
            eventLoop.race(autoRefresh(interval).compile.drain).void
          case None =>
            // Just run event loop
            eventLoop
    )(
      // Release: cleanup terminal (always runs, even on error)
      _ =>
        for
          _ <- terminal.showCursor.handleErrorWith(_ => Async[F].unit)
          _ <- terminal.exitAlternateScreen.handleErrorWith(_ => Async[F].unit)
          _ <- terminal.disableRawMode.handleErrorWith(_ => Async[F].unit)
          _ <- terminal.clear.handleErrorWith(_ => Async[F].unit)
        yield ()
    )

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
