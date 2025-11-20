package com.github.actions.ui

import com.github.actions.domain.{WorkflowRun, Job, RunFilter}
import java.time.Instant

/** View mode for the dashboard */
enum ViewMode:
  case RunList        // List of workflow runs
  case RunDetail      // Detail view of a single run
  case JobDetail      // Detail view of a single job
  case Help           // Help screen
  case Assistant      // AI assistant panel

/** Dashboard state */
case class DashboardState(
  // Data
  runs: List[WorkflowRun] = List.empty,
  selectedRunIndex: Int = 0,
  selectedJobIndex: Int = 0,
  
  // View state
  viewMode: ViewMode = ViewMode.RunList,
  scrollOffset: Int = 0,
  
  // Filter state
  filter: Option[RunFilter] = None,
  filterInput: String = "",
  isFilterMode: Boolean = false,
  
  // Metadata
  lastRefresh: Option[Instant] = None,
  isLoading: Boolean = false,
  error: Option[String] = None,
  assistantEnabled: Boolean = false,
  assistantSuggestions: List[com.github.actions.domain.AssistantSuggestion] = List.empty,
  assistantSelectedIndex: Int = 0,
  assistantInfo: Option[String] = None,
  assistantVerbose: Boolean = false,
  assistantActionsOnly: Boolean = false,
  autoRefreshEnabled: Boolean = true,
  
  // Repository info
  owner: String = "",
  repo: String = ""
):
  /** Get the currently selected run */
  def selectedRun: Option[WorkflowRun] =
    filteredRuns.lift(selectedRunIndex)
  
  /** Get the currently selected job */
  def selectedJob: Option[Job] =
    selectedRun.flatMap(_.jobs.lift(selectedJobIndex))
  
  /** Get filtered runs */
  def filteredRuns: List[WorkflowRun] =
    filter match
      case Some(f) => runs.filter(f.matches)
      case None => runs
  
  /** Move selection up */
  def moveUp: DashboardState =
    viewMode match
      case ViewMode.RunList =>
        if selectedRunIndex > 0 then
          copy(selectedRunIndex = selectedRunIndex - 1)
        else
          this
      case ViewMode.RunDetail =>
        if selectedJobIndex > 0 then
          copy(selectedJobIndex = selectedJobIndex - 1)
        else
          this
      case ViewMode.Assistant =>
        if assistantSelectedIndex > 0 then copy(assistantSelectedIndex = assistantSelectedIndex - 1) else this
      case _ => this
  
  /** Move selection down */
  def moveDown: DashboardState =
    viewMode match
      case ViewMode.RunList =>
        if selectedRunIndex < filteredRuns.length - 1 then
          copy(selectedRunIndex = selectedRunIndex + 1)
        else
          this
      case ViewMode.RunDetail =>
        selectedRun match
          case Some(run) if selectedJobIndex < run.jobs.length - 1 =>
            copy(selectedJobIndex = selectedJobIndex + 1)
          case _ => this
      case ViewMode.Assistant =>
        if assistantSelectedIndex < assistantSuggestions.length - 1 then copy(assistantSelectedIndex = assistantSelectedIndex + 1) else this
      case _ => this
  
  /** Page up */
  def pageUp(pageSize: Int): DashboardState =
    viewMode match
      case ViewMode.RunList =>
        val newIndex = (selectedRunIndex - pageSize).max(0)
        copy(selectedRunIndex = newIndex)
      case _ => this
  
  /** Page down */
  def pageDown(pageSize: Int): DashboardState =
    viewMode match
      case ViewMode.RunList =>
        val newIndex = (selectedRunIndex + pageSize).min(filteredRuns.length - 1)
        copy(selectedRunIndex = newIndex)
      case _ => this
  
  /** Go to top */
  def goToTop: DashboardState =
    viewMode match
      case ViewMode.RunList => copy(selectedRunIndex = 0)
      case ViewMode.RunDetail => copy(selectedJobIndex = 0)
      case ViewMode.Assistant => copy(assistantSelectedIndex = 0)
      case _ => this
  
  /** Go to bottom */
  def goToBottom: DashboardState =
    viewMode match
      case ViewMode.RunList =>
        copy(selectedRunIndex = (filteredRuns.length - 1).max(0))
      case ViewMode.RunDetail =>
        selectedRun match
          case Some(run) => copy(selectedJobIndex = (run.jobs.length - 1).max(0))
          case None => this
      case ViewMode.Assistant => copy(assistantSelectedIndex = (assistantSuggestions.length - 1).max(0))
      case _ => this
  
  /** Select current item (drill down) */
  def select: DashboardState =
    viewMode match
      case ViewMode.RunList =>
        copy(viewMode = ViewMode.RunDetail, selectedJobIndex = 0)
      case ViewMode.RunDetail =>
        copy(viewMode = ViewMode.JobDetail)
      case _ => this
  
  /** Go back to previous view */
  def back: DashboardState =
    viewMode match
      case ViewMode.RunDetail =>
        copy(viewMode = ViewMode.RunList)
      case ViewMode.JobDetail =>
        copy(viewMode = ViewMode.RunDetail)
      case ViewMode.Help =>
        copy(viewMode = ViewMode.RunList)
      case _ => this
  
  /** Toggle help view */
  def toggleHelp: DashboardState =
    if viewMode == ViewMode.Help then
      copy(viewMode = ViewMode.RunList)
    else
      copy(viewMode = ViewMode.Help)

  def toggleAssistant: DashboardState =
    if viewMode == ViewMode.Assistant then
      copy(viewMode = ViewMode.RunList)
    else
      copy(viewMode = ViewMode.Assistant)

  def toggleAssistantVerbose: DashboardState =
    copy(assistantVerbose = !assistantVerbose)

  def toggleAssistantActionsOnly: DashboardState =
    copy(assistantActionsOnly = !assistantActionsOnly)
  
  /** Update runs */
  def updateRuns(newRuns: List[WorkflowRun]): DashboardState =
    copy(
      runs = newRuns,
      lastRefresh = Some(Instant.now()),
      isLoading = false,
      error = None
    )
  
  /** Set loading state */
  def setLoading(loading: Boolean): DashboardState =
    copy(isLoading = loading)
  
  /** Set error */
  def setError(err: String): DashboardState =
    copy(error = Some(err), isLoading = false)

  def toggleAutoRefresh: DashboardState =
    copy(autoRefreshEnabled = !autoRefreshEnabled)

