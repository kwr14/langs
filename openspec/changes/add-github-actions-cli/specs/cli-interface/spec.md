# CLI Interface Specification

## ADDED Requirements

### Requirement: Command-Line Argument Parsing

The system SHALL parse command-line arguments using the decline library to support multiple subcommands with options and flags.

#### Scenario: Dashboard command with no arguments
- **WHEN** user runs `gh-actions dashboard`
- **THEN** the interactive dashboard launches for the current git repository

#### Scenario: List command with filters
- **WHEN** user runs `gh-actions list --status=failure --branch=main`
- **THEN** the system displays workflow runs filtered by status and branch

#### Scenario: Show command with run ID
- **WHEN** user runs `gh-actions show 12345`
- **THEN** the system displays detailed information for workflow run 12345

#### Scenario: Invalid command
- **WHEN** user runs `gh-actions invalid-command`
- **THEN** the system displays an error message and usage help

### Requirement: Repository Auto-Detection

The system SHALL automatically detect the GitHub repository from the current git directory.

#### Scenario: Inside git repository
- **WHEN** user runs the CLI from within a git repository
- **THEN** the system extracts owner and repo name from the git remote URL

#### Scenario: Outside git repository
- **WHEN** user runs the CLI outside a git repository
- **THEN** the system prompts for owner and repo name or displays an error

#### Scenario: Multiple remotes
- **WHEN** git repository has multiple remotes
- **THEN** the system uses the 'origin' remote by default

### Requirement: Authentication Configuration

The system SHALL support GitHub authentication via environment variable or configuration file.

#### Scenario: GITHUB_TOKEN environment variable
- **WHEN** GITHUB_TOKEN environment variable is set
- **THEN** the system uses this token for API authentication

#### Scenario: Missing authentication
- **WHEN** no authentication token is provided
- **THEN** the system displays an error with instructions to set GITHUB_TOKEN

#### Scenario: Invalid token
- **WHEN** provided token is invalid or expired
- **THEN** the system displays authentication error with HTTP status code

### Requirement: Output Format Selection

The system SHALL support multiple output formats for non-interactive commands.

#### Scenario: Default table output
- **WHEN** user runs `gh-actions list`
- **THEN** the system displays results in a formatted table

#### Scenario: JSON output
- **WHEN** user runs `gh-actions list --format=json`
- **THEN** the system outputs results as JSON array

#### Scenario: YAML output
- **WHEN** user runs `gh-actions list --format=yaml`
- **THEN** the system outputs results as YAML document

### Requirement: Error Handling and User Feedback

The system SHALL provide clear error messages and exit codes for all failure scenarios.

#### Scenario: Network error
- **WHEN** GitHub API is unreachable
- **THEN** the system displays network error message and exits with code 1

#### Scenario: API rate limit exceeded
- **WHEN** GitHub API rate limit is exceeded
- **THEN** the system displays rate limit error with reset time and exits with code 2

#### Scenario: Resource not found
- **WHEN** requested workflow run does not exist
- **THEN** the system displays "not found" error and exits with code 3

### Requirement: Help and Documentation

The system SHALL provide comprehensive help text for all commands and options.

#### Scenario: Global help
- **WHEN** user runs `gh-actions --help`
- **THEN** the system displays all available commands with descriptions

#### Scenario: Command-specific help
- **WHEN** user runs `gh-actions list --help`
- **THEN** the system displays options and examples for the list command

#### Scenario: Version information
- **WHEN** user runs `gh-actions --version`
- **THEN** the system displays the application version and build information

### Requirement: Subcommand Implementation

The system SHALL implement the following subcommands with their respective options.

#### Scenario: Dashboard subcommand
- **WHEN** user runs `gh-actions dashboard [--refresh=5]`
- **THEN** the interactive dashboard launches with specified refresh interval

#### Scenario: List subcommand
- **WHEN** user runs `gh-actions list [--status=...] [--branch=...] [--actor=...] [--limit=30]`
- **THEN** the system lists workflow runs with applied filters

#### Scenario: Show subcommand
- **WHEN** user runs `gh-actions show <run-id> [--jobs] [--steps]`
- **THEN** the system displays run details with optional job and step information

#### Scenario: Restart subcommand
- **WHEN** user runs `gh-actions restart <run-id> [--failed-jobs-only]`
- **THEN** the system restarts the workflow or only failed jobs

#### Scenario: Cancel subcommand
- **WHEN** user runs `gh-actions cancel <run-id>`
- **THEN** the system cancels the running workflow

#### Scenario: Watch subcommand
- **WHEN** user runs `gh-actions watch <run-id>`
- **THEN** the system monitors the run and displays updates until completion

