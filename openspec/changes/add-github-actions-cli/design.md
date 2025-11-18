# GitHub Actions CLI Dashboard - Technical Design

## Context

This project creates a terminal-based GitHub Actions workflow monitoring tool using Scala 3 and the Typelevel ecosystem. The design prioritizes type safety, composability, and developer experience while leveraging GitHub's official OpenAPI specification for API correctness.

### Constraints
- Must work with GitHub's REST API v3 (rate limits: 5000 req/hour authenticated)
- Terminal UI must be responsive (<100ms input latency)
- Should auto-detect git repository context
- Must support both interactive and non-interactive modes
- Native binary should start in <1s

### Stakeholders
- Developers monitoring CI/CD pipelines
- DevOps engineers debugging workflow failures
- Teams wanting terminal-based workflow management

## Goals / Non-Goals

### Goals
- Real-time interactive dashboard with auto-refresh
- Type-safe GitHub API integration via OpenAPI
- Pure functional architecture with testable components
- Fast native binary distribution
- Intuitive keyboard-driven UX

### Non-Goals
- Web-based UI (terminal only)
- Workflow file editing (read-only operations + restart/cancel)
- GitHub Enterprise Server support (GitHub.com only in v1)
- Workflow creation/modification (management only)
- Multi-repository dashboard (single repo focus)

## Architecture

### Module Structure

```
github-actions-cli/
├── core/                    # Domain models and business logic
│   ├── domain/             # Workflow, Run, Job, Step models
│   ├── service/            # WorkflowService, DashboardService
│   └── algebra/            # Effect interfaces (GitHubClient[F], Terminal[F])
├── api-client/             # GitHub API integration
│   ├── generated/          # OpenAPI-generated code
│   ├── client/             # Http4s client implementation
│   └── auth/               # Token authentication
├── terminal-ui/            # TUI components
│   ├── components/         # Dashboard, WorkflowList, JobProgress
│   ├── renderer/           # Terminal rendering logic
│   └── input/              # Keyboard event handling
├── cli/                    # CLI entry point
│   ├── commands/           # Subcommand implementations
│   └── Main.scala          # Application entry point
└── tests/
    ├── unit/               # Pure logic tests
    └── integration/        # API client tests (mocked)
```

### Technology Stack

#### Core Libraries
- **Scala 3.5.0**: Latest stable with enums, union types, opaque types
- **cats-effect 3.5.4**: Effect system and concurrency primitives
- **fs2 3.10.2**: Streaming for auto-refresh and event handling

#### HTTP & API
- **http4s-ember-client 0.23.27**: HTTP client
- **circe 0.14.10**: JSON parsing and encoding
- **sttp-openapi-generator**: Generate client from GitHub OpenAPI spec

#### CLI & Terminal
- **decline 2.4.1**: Command-line parsing
- **tui-scala 0.1.0** or **crossterm-scala**: Terminal UI rendering
- **fansi 0.5.0**: ANSI color codes

#### Build & Distribution
- **sbt 1.10.0**: Build tool
- **sbt-native-image**: GraalVM native-image plugin
- **sbt-assembly**: Fat JAR creation

## Decisions

### Decision 1: OpenAPI-Driven API Client

**What**: Generate GitHub API client from official OpenAPI specification

**Why**:
- Ensures type safety and correctness
- Automatic updates when GitHub API changes
- Reduces manual coding errors
- Self-documenting API surface

**Alternatives Considered**:
- Manual client implementation: Error-prone, hard to maintain
- Existing GitHub libraries: Not Scala 3 compatible, less type-safe

**Implementation**:
```scala
// Generated from OpenAPI spec
trait GitHubClient[F[_]]:
  def listWorkflowRuns(owner: String, repo: String, params: ListParams): F[WorkflowRunsResponse]
  def getWorkflowRun(owner: String, repo: String, runId: Long): F[WorkflowRun]
  def rerunWorkflow(owner: String, repo: String, runId: Long): F[Unit]
  def cancelWorkflowRun(owner: String, repo: String, runId: Long): F[Unit]
```

### Decision 2: Effect Algebra Pattern

**What**: Define effect interfaces (algebras) for external dependencies

**Why**:
- Enables testing with mock implementations
- Decouples business logic from infrastructure
- Supports multiple effect types (IO, test effects)

**Example**:
```scala
trait Terminal[F[_]]:
  def render(screen: Screen): F[Unit]
  def readKey: F[KeyEvent]
  def clear: F[Unit]
  def size: F[(Int, Int)]

trait GitRepository[F[_]]:
  def currentRepo: F[Option[(String, String)]]  // (owner, repo)
  def currentBranch: F[Option[String]]
```

### Decision 3: Component-Based Dashboard

**What**: Compose dashboard from reusable UI components

**Why**:
- Testable rendering logic
- Reusable across different views
- Clear separation of concerns

**Components**:
```scala
trait Component[F[_]]:
  def render(state: State, bounds: Rect): F[RenderTree]

case class Dashboard[F[_]](
  header: HeaderComponent[F],
  summary: SummaryComponent[F],
  workflowList: WorkflowListComponent[F],
  activeJobs: ActiveJobsComponent[F],
  footer: FooterComponent[F]
) extends Component[F]
```

### Decision 4: State Management with Ref

**What**: Use cats-effect Ref for mutable state in dashboard

**Why**:
- Thread-safe state updates
- Composable with other effects
- Testable without mocking

**Example**:
```scala
case class DashboardState(
  runs: List[WorkflowRun],
  selectedIndex: Int,
  filter: RunFilter,
  lastUpdate: Instant
)

class DashboardService[F[_]: Async](
  client: GitHubClient[F],
  stateRef: Ref[F, DashboardState]
):
  def refresh: F[Unit] =
    for
      state <- stateRef.get
      runs <- client.listWorkflowRuns(owner, repo, state.filter.toParams)
      _ <- stateRef.update(_.copy(runs = runs, lastUpdate = Instant.now))
    yield ()
```

### Decision 5: Auto-Refresh with fs2 Streams

**What**: Use fs2 streams for periodic dashboard refresh

**Why**:
- Composable with other streams (keyboard input)
- Resource-safe cancellation
- Backpressure handling

**Example**:
```scala
def autoRefresh(interval: FiniteDuration): Stream[F, Unit] =
  Stream.fixedRate[F](interval).evalMap(_ => service.refresh)

def keyboardEvents: Stream[F, KeyEvent] =
  Stream.repeatEval(terminal.readKey)

def dashboardLoop: Stream[F, Unit] =
  autoRefresh(1.second)
    .merge(keyboardEvents.evalMap(handleKey))
    .evalMap(_ => render)
```

## Data Models

### Core Domain

```scala
enum WorkflowStatus:
  case Queued, InProgress, Completed

enum WorkflowConclusion:
  case Success, Failure, Cancelled, Skipped, TimedOut, ActionRequired, Neutral

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
  jobs: List[Job]
)

case class Job(
  id: Long,
  name: String,
  status: WorkflowStatus,
  conclusion: Option[WorkflowConclusion],
  startedAt: Option[Instant],
  completedAt: Option[Instant],
  steps: List[Step]
)

case class Step(
  name: String,
  status: WorkflowStatus,
  conclusion: Option[WorkflowConclusion],
  number: Int
)
```

## Risks / Trade-offs

### Risk 1: GitHub API Rate Limits
**Impact**: Dashboard refresh may hit rate limits (5000 req/hour)

**Mitigation**:
- Configurable refresh interval (default 5s)
- Conditional requests using ETags
- Display rate limit status in dashboard
- Warn user when approaching limit

### Risk 2: Terminal Compatibility
**Impact**: Different terminals may render differently

**Mitigation**:
- Test on major terminals (iTerm2, Terminal.app, Alacritty, Windows Terminal)
- Fallback to simple ASCII rendering if Unicode unavailable
- Detect terminal capabilities at startup

### Risk 3: Large Workflow Runs
**Impact**: Workflows with 100+ jobs may be slow to render

**Mitigation**:
- Pagination for workflow list
- Lazy loading of job details
- Virtual scrolling for large lists
- Limit initial fetch to 30 runs

### Risk 4: GraalVM Native Image Compatibility
**Impact**: Some libraries may not work with native-image

**Mitigation**:
- Test native-image build early
- Provide reflection configuration for problematic libraries
- Fallback to JVM JAR if native-image fails
- Document known limitations

## Migration Plan

Not applicable - new project with no existing users.

## Deployment Strategy

### Phase 1: MVP (Weeks 1-2)
- Basic CLI with `list` and `show` commands
- GitHub API client (generated from OpenAPI)
- Simple table output (no TUI)
- Authentication via GITHUB_TOKEN

### Phase 2: Interactive Dashboard (Weeks 3-4)
- Terminal UI components
- Real-time auto-refresh
- Keyboard navigation
- Color-coded status indicators

### Phase 3: Advanced Features (Weeks 5-6)
- Restart/cancel operations
- Watch mode for specific runs
- Progress bars for running jobs
- Filter and search capabilities

### Phase 4: Distribution (Week 7)
- GraalVM native-image build
- GitHub release with binaries
- Homebrew formula
- Documentation and examples

## Open Questions

1. **Terminal UI Library**: tui-scala vs crossterm-scala vs custom implementation?
   - Need to evaluate maturity and Scala 3 compatibility
   - Fallback: Build minimal custom renderer with fansi

2. **OpenAPI Generator**: Which sbt plugin works best with Scala 3?
   - Options: guardrail, openapi-generator, smithy4s
   - Need to test with GitHub's OpenAPI spec

3. **Configuration**: Environment variables vs config file vs both?
   - Proposal: Environment variables for tokens, config file for preferences

4. **Error Handling**: How to display API errors in dashboard?
   - Proposal: Error banner at top of dashboard, log to file for details

5. **Testing Strategy**: How to test terminal rendering?
   - Proposal: Snapshot testing for render output, property-based for state transitions



