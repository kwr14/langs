# Workflow Management Specification

## ADDED Requirements

### Requirement: Workflow Run Listing

The system SHALL provide functionality to list and filter workflow runs.

#### Scenario: List all workflow runs
- **WHEN** service lists workflow runs without filters
- **THEN** the system returns all recent workflow runs for the repository

#### Scenario: Filter by status
- **WHEN** service lists workflow runs with status filter
- **THEN** the system returns only runs matching the specified status

#### Scenario: Filter by branch
- **WHEN** service lists workflow runs with branch filter
- **THEN** the system returns only runs from the specified branch

#### Scenario: Filter by actor
- **WHEN** service lists workflow runs with actor filter
- **THEN** the system returns only runs triggered by the specified user

#### Scenario: Limit results
- **WHEN** service lists workflow runs with limit parameter
- **THEN** the system returns at most the specified number of runs

### Requirement: Workflow Run Details

The system SHALL provide detailed information about workflow runs.

#### Scenario: Get run summary
- **WHEN** service retrieves workflow run details
- **THEN** the system returns run metadata including status, conclusion, timing, and actor

#### Scenario: Get run jobs
- **WHEN** service retrieves workflow run with jobs
- **THEN** the system returns all jobs with their status and steps

#### Scenario: Calculate run duration
- **WHEN** service retrieves completed workflow run
- **THEN** the system calculates and returns total duration

#### Scenario: Get run logs URL
- **WHEN** service retrieves workflow run details
- **THEN** the system includes URLs for downloading logs

### Requirement: Workflow Run Actions

The system SHALL support actions on workflow runs.

#### Scenario: Restart entire workflow
- **WHEN** service restarts a workflow run
- **THEN** the system triggers a complete rerun of all jobs

#### Scenario: Restart failed jobs only
- **WHEN** service restarts failed jobs
- **THEN** the system triggers rerun of only jobs that failed

#### Scenario: Cancel running workflow
- **WHEN** service cancels a workflow run
- **THEN** the system stops all in-progress jobs

#### Scenario: Action on completed workflow
- **WHEN** service attempts to cancel a completed workflow
- **THEN** the system raises an error indicating workflow is not running

### Requirement: Job Management

The system SHALL provide job-level information and operations.

#### Scenario: List jobs for run
- **WHEN** service lists jobs for a workflow run
- **THEN** the system returns all jobs with their status and metadata

#### Scenario: Get job details
- **WHEN** service retrieves job details
- **THEN** the system returns job information including all steps

#### Scenario: Calculate job progress
- **WHEN** service retrieves running job
- **THEN** the system calculates progress as completed steps / total steps

#### Scenario: Get job logs
- **WHEN** service retrieves job logs
- **THEN** the system returns log content for the job

### Requirement: Step Information

The system SHALL provide step-level information for jobs.

#### Scenario: List steps for job
- **WHEN** service retrieves job details
- **THEN** the system includes all steps with their status and timing

#### Scenario: Step status tracking
- **WHEN** service monitors running job
- **THEN** the system tracks which step is currently executing

#### Scenario: Step duration calculation
- **WHEN** service retrieves completed step
- **THEN** the system calculates step duration from start and end times

### Requirement: Statistics and Aggregation

The system SHALL calculate aggregate statistics for workflow runs.

#### Scenario: Success rate calculation
- **WHEN** service calculates success rate
- **THEN** the system returns percentage of successful runs from recent runs

#### Scenario: Average duration calculation
- **WHEN** service calculates average duration
- **THEN** the system returns mean duration of completed runs

#### Scenario: Failure analysis
- **WHEN** service analyzes failures
- **THEN** the system groups failures by job name and provides counts

#### Scenario: Active runs count
- **WHEN** service counts active runs
- **THEN** the system returns number of currently running workflows

### Requirement: Watch Mode

The system SHALL support monitoring a specific workflow run until completion.

#### Scenario: Watch running workflow
- **WHEN** service watches a running workflow
- **THEN** the system polls for updates and emits status changes

#### Scenario: Watch completion
- **WHEN** watched workflow completes
- **THEN** the system emits final status and stops watching

#### Scenario: Watch timeout
- **WHEN** watched workflow exceeds timeout duration
- **THEN** the system stops watching and raises timeout error

### Requirement: Caching and State Management

The system SHALL cache workflow data to minimize API calls.

#### Scenario: Cache workflow runs
- **WHEN** service fetches workflow runs
- **THEN** the system caches results with timestamp

#### Scenario: Cache invalidation
- **WHEN** cached data exceeds max age
- **THEN** the system fetches fresh data from API

#### Scenario: Conditional refresh
- **WHEN** service refreshes cached data
- **THEN** the system uses ETags to avoid unnecessary data transfer

### Requirement: Error Handling

The system SHALL handle errors gracefully and provide meaningful messages.

#### Scenario: Workflow not found
- **WHEN** service retrieves non-existent workflow run
- **THEN** the system raises NotFoundError with run ID

#### Scenario: Permission denied
- **WHEN** service attempts action without permissions
- **THEN** the system raises PermissionError with required permission

#### Scenario: Network failure
- **WHEN** API request fails due to network error
- **THEN** the system raises NetworkError with retry suggestion

#### Scenario: Invalid state transition
- **WHEN** service attempts invalid action (e.g., restart running workflow)
- **THEN** the system raises InvalidStateError with explanation

### Requirement: Concurrent Operations

The system SHALL handle concurrent workflow operations safely.

#### Scenario: Concurrent refreshes
- **WHEN** multiple refresh requests occur simultaneously
- **THEN** the system deduplicates requests and shares results

#### Scenario: State consistency
- **WHEN** workflow state changes during operation
- **THEN** the system detects conflicts and retries or fails gracefully

### Requirement: Repository Context

The system SHALL manage repository context for all operations.

#### Scenario: Repository detection
- **WHEN** service initializes
- **THEN** the system detects repository from git configuration

#### Scenario: Explicit repository specification
- **WHEN** user specifies repository explicitly
- **THEN** the system uses provided owner and repo name

#### Scenario: Repository validation
- **WHEN** service validates repository
- **THEN** the system checks if repository exists and is accessible

