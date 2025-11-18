# Terminal Dashboard Specification

## ADDED Requirements

### Requirement: Interactive Dashboard Layout

The system SHALL render an interactive terminal dashboard with multiple components.

#### Scenario: Dashboard initialization
- **WHEN** dashboard launches
- **THEN** the system displays header, summary, workflow list, active jobs, and footer sections

#### Scenario: Responsive layout
- **WHEN** terminal is resized
- **THEN** the system adjusts component sizes to fit the new dimensions

#### Scenario: Minimum terminal size
- **WHEN** terminal is smaller than 80x24 characters
- **THEN** the system displays a warning message about minimum size requirements

### Requirement: Header Component

The system SHALL display repository information and status in the header.

#### Scenario: Repository information display
- **WHEN** dashboard renders header
- **THEN** the system displays repository owner, name, and current branch

#### Scenario: Last update timestamp
- **WHEN** dashboard refreshes
- **THEN** the system updates the "Last updated" timestamp in the header

#### Scenario: Connection status indicator
- **WHEN** API connection status changes
- **THEN** the system displays connected/disconnected indicator in header

### Requirement: Summary Statistics Component

The system SHALL display aggregate statistics for workflow runs.

#### Scenario: Success rate calculation
- **WHEN** dashboard renders summary
- **THEN** the system displays percentage of successful runs from recent runs

#### Scenario: Active runs count
- **WHEN** dashboard renders summary
- **THEN** the system displays count of currently running workflows

#### Scenario: Average duration
- **WHEN** dashboard renders summary
- **THEN** the system displays average duration of completed runs

### Requirement: Workflow List Component

The system SHALL display a scrollable list of workflow runs with status indicators.

#### Scenario: Workflow run display
- **WHEN** dashboard renders workflow list
- **THEN** each run shows name, status, branch, actor, and duration

#### Scenario: Color-coded status
- **WHEN** workflow run has a status
- **THEN** the system displays it with appropriate color (green=success, red=failure, yellow=in_progress, gray=cancelled)

#### Scenario: Selection highlighting
- **WHEN** user navigates workflow list
- **THEN** the system highlights the currently selected run

#### Scenario: Scrolling behavior
- **WHEN** workflow list exceeds visible area
- **THEN** the system enables scrolling with arrow keys

### Requirement: Active Jobs Component

The system SHALL display progress bars for currently running jobs.

#### Scenario: Job progress display
- **WHEN** jobs are running
- **THEN** the system displays progress bars showing completed vs total steps

#### Scenario: Job name and status
- **WHEN** rendering active job
- **THEN** the system displays job name, current step, and elapsed time

#### Scenario: No active jobs
- **WHEN** no jobs are running
- **THEN** the system displays "No active jobs" message

### Requirement: Footer Component

The system SHALL display keyboard shortcuts and help information in the footer.

#### Scenario: Keyboard shortcuts display
- **WHEN** dashboard renders footer
- **THEN** the system displays available keyboard shortcuts (q=quit, r=refresh, etc.)

#### Scenario: Context-sensitive help
- **WHEN** user selects different components
- **THEN** the system updates footer to show relevant shortcuts

### Requirement: Keyboard Navigation

The system SHALL support keyboard-driven navigation and actions.

#### Scenario: Arrow key navigation
- **WHEN** user presses up/down arrow keys
- **THEN** the system moves selection in workflow list

#### Scenario: Enter key action
- **WHEN** user presses Enter on selected workflow
- **THEN** the system displays detailed view of the workflow run

#### Scenario: Refresh action
- **WHEN** user presses 'r' key
- **THEN** the system immediately refreshes dashboard data

#### Scenario: Quit action
- **WHEN** user presses 'q' key
- **THEN** the system exits the dashboard gracefully

#### Scenario: Filter toggle
- **WHEN** user presses 'f' key
- **THEN** the system opens filter input dialog

### Requirement: Auto-Refresh Mechanism

The system SHALL automatically refresh dashboard data at configurable intervals.

#### Scenario: Periodic refresh
- **WHEN** auto-refresh is enabled
- **THEN** the system fetches new data every N seconds (default 5)

#### Scenario: Refresh interval configuration
- **WHEN** user specifies --refresh flag
- **THEN** the system uses the specified interval in seconds

#### Scenario: Pause auto-refresh
- **WHEN** user presses 'p' key
- **THEN** the system pauses auto-refresh until resumed

### Requirement: Color and Styling

The system SHALL use ANSI colors and Unicode characters for visual clarity.

#### Scenario: Status color coding
- **WHEN** rendering workflow status
- **THEN** the system uses green for success, red for failure, yellow for in_progress

#### Scenario: Unicode symbols
- **WHEN** terminal supports Unicode
- **THEN** the system uses symbols (✓, ✗, ⟳, ⏸) for status indicators

#### Scenario: ASCII fallback
- **WHEN** terminal does not support Unicode
- **THEN** the system falls back to ASCII characters ([OK], [FAIL], etc.)

### Requirement: Detail View

The system SHALL provide a detailed view for selected workflow runs.

#### Scenario: Workflow detail display
- **WHEN** user selects a workflow and presses Enter
- **THEN** the system displays full run details including all jobs and steps

#### Scenario: Job expansion
- **WHEN** user navigates to a job in detail view
- **THEN** the system expands to show all steps with their status

#### Scenario: Return to list view
- **WHEN** user presses Escape in detail view
- **THEN** the system returns to the main dashboard list

### Requirement: Filter and Search

The system SHALL support filtering workflow runs in the dashboard.

#### Scenario: Status filter
- **WHEN** user applies status filter
- **THEN** the system displays only runs matching the selected status

#### Scenario: Branch filter
- **WHEN** user applies branch filter
- **THEN** the system displays only runs from the specified branch

#### Scenario: Clear filters
- **WHEN** user presses 'c' key
- **THEN** the system clears all active filters

### Requirement: Error Display

The system SHALL display errors and warnings in the dashboard.

#### Scenario: API error banner
- **WHEN** API request fails
- **THEN** the system displays error banner at top of dashboard with error message

#### Scenario: Rate limit warning
- **WHEN** rate limit is below 100 requests
- **THEN** the system displays warning in header with remaining requests

#### Scenario: Dismissible errors
- **WHEN** user presses 'd' key on error banner
- **THEN** the system dismisses the error message

