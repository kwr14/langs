# Add GitHub Actions Workflow CLI Dashboard

## Why

GitHub Actions workflows are critical to modern CI/CD pipelines, but monitoring and managing them requires switching between browser tabs, navigating GitHub's web UI, and manually refreshing pages. Developers need a fast, terminal-based tool to monitor workflow runs in real-time, drill down into failures, and take action (restart, cancel) without leaving their development environment.

This project fills the gap between GitHub's web UI (feature-rich but slow for monitoring) and the GitHub CLI (powerful but lacks interactive dashboard capabilities). It provides a developer-focused, real-time monitoring experience optimized for rapid feedback during development and debugging.

## What Changes

This change introduces a new Scala 3 CLI application with the following capabilities:

- **Interactive Terminal Dashboard**: Real-time, auto-refreshing view of all workflow runs with color-coded status indicators, progress bars for running jobs, and keyboard-driven navigation
- **OpenAPI-Driven GitHub API Client**: Type-safe client generated from GitHub's official REST API specification, ensuring correctness and maintainability
- **Workflow Management Operations**: List, filter, view details, restart failed jobs, and cancel running workflows
- **Pure Functional Architecture**: Built on Typelevel stack (cats-effect, http4s, circe) with referential transparency and composable abstractions
- **Native Binary Distribution**: GraalVM native-image support for fast startup and low resource usage
- **Multiple Output Formats**: Support for table, JSON, and YAML output for scripting and integration

### Key Features

1. **Dashboard Components**:
   - Header: Repository info, last update timestamp
   - Summary: Success rate, average duration, active runs
   - Workflow List: Scrollable list with status, branch, actor, duration
   - Active Jobs: Real-time progress bars for in-progress jobs
   - Footer: Keyboard shortcuts and help

2. **CLI Commands**:
   - `gh-actions dashboard` - Launch interactive dashboard
   - `gh-actions list [--status=...] [--branch=...] [--actor=...]` - List workflow runs
   - `gh-actions show <run-id>` - Show detailed run information
   - `gh-actions restart <run-id>` - Restart failed workflow
   - `gh-actions cancel <run-id>` - Cancel running workflow
   - `gh-actions watch <run-id>` - Watch specific run until completion

3. **Technical Approach**:
   - Scala 3 with latest features (enums, union types, opaque types)
   - OpenAPI code generation for GitHub REST API
   - Terminal UI using tui-scala or similar library
   - State management with cats-effect Ref
   - HTTP client with http4s-ember-client
   - JSON parsing with circe
   - CLI parsing with decline
   - Configuration via environment variables and git repository detection

## Impact

### New Capabilities
- `cli-interface`: Command-line interface with subcommands and options
- `github-api-client`: Type-safe GitHub REST API client
- `terminal-dashboard`: Interactive TUI with real-time updates
- `workflow-management`: Business logic for workflow operations

### Affected Code
- New project under `scala/github-actions-cli/`
- New build configuration: `scala/github-actions-cli/build.sbt`
- New GitHub workflow: `.github/workflows/scala-github-actions-cli.yml`

### Dependencies
- cats-effect 3.5+
- http4s 0.23+
- circe 0.14+
- decline 2.4+
- tui-scala or crossterm-scala for terminal UI
- OpenAPI generator plugin for sbt

### Breaking Changes
None - this is a new standalone project.

### Migration Path
Not applicable - new project.

### Success Criteria
- Interactive dashboard displays workflow runs with <1s refresh rate
- All GitHub Actions API operations work correctly
- Type-safe API client prevents runtime errors
- Native binary builds successfully with GraalVM
- Can monitor workflows from any git repository
- Keyboard navigation is intuitive and responsive
- Tests cover core business logic and API client

