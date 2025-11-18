# GitHub Actions Workflow CLI Dashboard - Project Plan

## Executive Summary

This document provides a comprehensive plan for building a Scala 3 CLI tool that serves as a GitHub Actions workflow utility, featuring an interactive terminal dashboard to monitor and manage workflow runs.

**Project Location**: `scala/github-actions-cli/`  
**OpenSpec Change**: `openspec/changes/add-github-actions-cli/`  
**Timeline**: 7 weeks (MVP to production-ready)  
**Tech Stack**: Scala 3.5, Typelevel (cats-effect, http4s, fs2, circe), decline

## Quick Links

- **Proposal**: [openspec/changes/add-github-actions-cli/proposal.md](../../openspec/changes/add-github-actions-cli/proposal.md)
- **Design**: [openspec/changes/add-github-actions-cli/design.md](../../openspec/changes/add-github-actions-cli/design.md)
- **Tasks**: [openspec/changes/add-github-actions-cli/tasks.md](../../openspec/changes/add-github-actions-cli/tasks.md)
- **Specifications**: [openspec/changes/add-github-actions-cli/specs/](../../openspec/changes/add-github-actions-cli/specs/)

## Project Vision

### Problem Statement

Developers monitoring GitHub Actions workflows face several pain points:
- Constant browser tab switching and manual page refreshing
- Slow web UI for rapid iteration during debugging
- No terminal-based real-time monitoring solution
- GitHub CLI lacks interactive dashboard capabilities

### Solution

A terminal-based CLI tool that provides:
- **Real-time interactive dashboard** with auto-refresh
- **Type-safe GitHub API integration** via OpenAPI
- **Keyboard-driven workflow management** (list, view, restart, cancel)
- **Fast native binary** with <1s startup time
- **Pure functional architecture** for reliability and testability

## Architecture Overview

### Module Structure

```
github-actions-cli/
├── core/                    # Domain models and business logic
│   ├── domain/             # WorkflowRun, Job, Step, Actor models
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
| Library | Version | Purpose |
|---------|---------|---------|
| Scala | 3.5.0 | Language with enums, union types, opaque types |
| cats-effect | 3.5.4 | Effect system and concurrency |
| fs2 | 3.10.2 | Streaming for auto-refresh and events |

#### HTTP & API
| Library | Version | Purpose |
|---------|---------|---------|
| http4s-ember-client | 0.23.27 | HTTP client |
| circe | 0.14.10 | JSON parsing and encoding |
| sttp-openapi-generator | TBD | Generate client from GitHub OpenAPI spec |

#### CLI & Terminal
| Library | Version | Purpose |
|---------|---------|---------|
| decline | 2.4.1 | Command-line parsing |
| tui-scala / crossterm-scala | TBD | Terminal UI rendering |
| fansi | 0.5.0 | ANSI color codes |

#### Build & Distribution
| Tool | Version | Purpose |
|------|---------|---------|
| sbt | 1.10.0 | Build tool |
| sbt-native-image | Latest | GraalVM native-image plugin |
| sbt-assembly | Latest | Fat JAR creation |

## Key Design Decisions

### 1. OpenAPI-Driven API Client

**Decision**: Generate GitHub API client from official OpenAPI specification

**Rationale**:
- Ensures type safety and correctness
- Automatic updates when GitHub API changes
- Reduces manual coding errors
- Self-documenting API surface

**Implementation**:
```scala
trait GitHubClient[F[_]]:
  def listWorkflowRuns(owner: String, repo: String, params: ListParams): F[WorkflowRunsResponse]
  def getWorkflowRun(owner: String, repo: String, runId: Long): F[WorkflowRun]
  def rerunWorkflow(owner: String, repo: String, runId: Long): F[Unit]
  def cancelWorkflowRun(owner: String, repo: String, runId: Long): F[Unit]
```

### 2. Effect Algebra Pattern

**Decision**: Define effect interfaces (algebras) for external dependencies

**Rationale**:
- Enables testing with mock implementations
- Decouples business logic from infrastructure
- Supports multiple effect types

**Example**:
```scala
trait Terminal[F[_]]:
  def render(screen: Screen): F[Unit]
  def readKey: F[KeyEvent]
  def clear: F[Unit]
  def size: F[(Int, Int)]
```

### 3. Component-Based Dashboard

**Decision**: Compose dashboard from reusable UI components

**Rationale**:
- Testable rendering logic
- Reusable across different views
- Clear separation of concerns

### 4. State Management with Ref

**Decision**: Use cats-effect Ref for mutable state

**Rationale**:
- Thread-safe state updates
- Composable with other effects
- Testable without mocking

### 5. Auto-Refresh with fs2 Streams

**Decision**: Use fs2 streams for periodic dashboard refresh

**Rationale**:
- Composable with other streams (keyboard input)
- Resource-safe cancellation
- Backpressure handling

## Dashboard Layout

```
┌─────────────────────────────────────────────────────────────────────┐
│ GitHub Actions Dashboard - owner/repo (main)    Last: 2s ago  [●]  │
├─────────────────────────────────────────────────────────────────────┤
│ Active: 2  Success: 85%  Avg Duration: 3m 24s  Rate Limit: 4,892  │
├─────────────────────────────────────────────────────────────────────┤
│ Workflows                                                           │
│ ✓ Build and Test      main    @user   2m 15s   5 mins ago         │
│ ⟳ Deploy Production   main    @user   1m 30s   Running...         │
│ ✗ Integration Tests   feat-x  @dev    4m 02s   10 mins ago        │
│ ✓ Lint                main    @user   45s      15 mins ago        │
│                                                                     │
├─────────────────────────────────────────────────────────────────────┤
│ Active Jobs                                                         │
│ Deploy to AWS    [████████░░░░░░░░] 8/15 steps  Elapsed: 1m 30s   │
│ Run Tests        [██████░░░░░░░░░░] 6/12 steps  Elapsed: 45s      │
├─────────────────────────────────────────────────────────────────────┤
│ q:Quit r:Refresh ↑↓:Navigate ⏎:Details f:Filter p:Pause           │
└─────────────────────────────────────────────────────────────────────┘
```

## CLI Commands

### Dashboard Command
```bash
gh-actions dashboard [--refresh=5]
```
Launch interactive dashboard with optional refresh interval (default: 5 seconds)

### List Command
```bash
gh-actions list [OPTIONS]

Options:
  --status=<status>      Filter by status (queued, in_progress, completed)
  --branch=<branch>      Filter by branch name
  --actor=<username>     Filter by actor username
  --limit=<n>            Limit results (default: 30)
  --format=<format>      Output format (table, json, yaml)
```

### Show Command
```bash
gh-actions show <run-id> [OPTIONS]

Options:
  --jobs                 Include job details
  --steps                Include step details
  --format=<format>      Output format (table, json, yaml)
```

### Restart Command
```bash
gh-actions restart <run-id> [OPTIONS]

Options:
  --failed-jobs-only     Restart only failed jobs
```

### Cancel Command
```bash
gh-actions cancel <run-id>
```

### Watch Command
```bash
gh-actions watch <run-id> [OPTIONS]

Options:
  --timeout=<seconds>    Maximum watch duration (default: 3600)
```

## Implementation Phases

### Phase 1: Project Setup and Foundation (Week 1)
**Goal**: Establish project structure and core domain models

**Deliverables**:
- Project directory structure
- build.sbt with all dependencies
- Core domain models (WorkflowRun, Job, Step, etc.)
- Basic test setup

**Key Tasks**:
- Create module structure (core, api-client, terminal-ui, cli)
- Add Typelevel dependencies
- Define Scala 3 enums for status and conclusion
- Set up testing framework

### Phase 2: GitHub API Client (Week 2)
**Goal**: Type-safe GitHub API integration

**Deliverables**:
- OpenAPI-generated client code
- HTTP client implementation with http4s
- Authentication support
- Error handling and retry logic
- Rate limit tracking

**Key Tasks**:
- Download and configure GitHub OpenAPI spec
- Generate client code
- Implement API methods (list, get, rerun, cancel)
- Add ETag caching support
- Write integration tests with mocked responses

### Phase 3: CLI Interface (Week 3)
**Goal**: Command-line interface with all subcommands

**Deliverables**:
- Main entry point with decline
- All subcommands implemented
- Repository auto-detection
- Multiple output formats (table, JSON, YAML)
- Configuration management

**Key Tasks**:
- Set up decline command structure
- Implement each subcommand
- Add git config parsing for repository detection
- Create formatters for different output types
- Add environment variable support (GITHUB_TOKEN)

### Phase 4: Terminal Dashboard (Week 4)
**Goal**: Interactive TUI with real-time updates

**Deliverables**:
- Terminal UI framework
- All dashboard components
- Keyboard navigation
- Color-coded status indicators
- Responsive layout

**Key Tasks**:
- Evaluate and integrate terminal UI library
- Build component hierarchy
- Implement rendering logic
- Add keyboard event handling
- Create progress bar visualization

### Phase 5: Workflow Management Service (Week 5)
**Goal**: Business logic layer

**Deliverables**:
- WorkflowService implementation
- DashboardService with auto-refresh
- Caching layer
- Statistics calculations
- Watch mode

**Key Tasks**:
- Implement service layer
- Add caching with Ref
- Create auto-refresh with fs2 streams
- Implement filter logic
- Add watch mode polling

### Phase 6: Integration and Advanced Features (Week 6)
**Goal**: Wire everything together and add polish

**Deliverables**:
- Fully integrated dashboard
- Action commands (restart, cancel)
- Error handling and display
- Comprehensive tests
- Performance optimizations

**Key Tasks**:
- Integrate dashboard with services
- Implement action confirmations
- Add error banners and warnings
- Write unit and integration tests
- Optimize rendering and API calls

### Phase 7: Distribution and Documentation (Week 7)
**Goal**: Production-ready release

**Deliverables**:
- GraalVM native binary
- Fat JAR distribution
- GitHub Actions CI/CD
- Comprehensive documentation
- v0.1.0 release

**Key Tasks**:
- Configure native-image build
- Set up GitHub Actions workflow
- Write README and user guide
- Create installation instructions
- Prepare release artifacts

## Data Models

### Core Domain Models

```scala
// Status and Conclusion Enums
enum WorkflowStatus:
  case Queued, InProgress, Completed

enum WorkflowConclusion:
  case Success, Failure, Cancelled, Skipped, TimedOut, ActionRequired, Neutral

// Main Domain Models
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

case class Actor(
  login: String,
  avatarUrl: String
)

case class Repository(
  owner: String,
  name: String
)
```

### Dashboard State

```scala
case class DashboardState(
  runs: List[WorkflowRun],
  selectedIndex: Int,
  filter: RunFilter,
  lastUpdate: Instant,
  error: Option[String]
)

case class RunFilter(
  status: Option[WorkflowStatus],
  branch: Option[String],
  actor: Option[String]
)
```

## Risk Management

### Risk 1: GitHub API Rate Limits
**Impact**: High - Dashboard refresh may hit rate limits (5000 req/hour authenticated)

**Mitigation**:
- Configurable refresh interval (default 5s, recommend 10s for heavy use)
- Conditional requests using ETags (304 responses don't count against limit)
- Display rate limit status in dashboard header
- Warn user when approaching limit (<100 remaining)
- Cache responses with timestamps

### Risk 2: Terminal Compatibility
**Impact**: Medium - Different terminals may render differently

**Mitigation**:
- Test on major terminals (iTerm2, Terminal.app, Alacritty, Windows Terminal)
- Fallback to ASCII rendering if Unicode unavailable
- Detect terminal capabilities at startup
- Document minimum terminal requirements (80x24)

### Risk 3: OpenAPI Generator Compatibility
**Impact**: Medium - Generator may not work well with Scala 3

**Mitigation**:
- Evaluate multiple generators early (guardrail, openapi-generator, smithy4s)
- Fallback to manual client implementation if needed
- Keep API client interface separate from implementation
- Document generator choice and rationale

### Risk 4: GraalVM Native Image Compatibility
**Impact**: Medium - Some libraries may not work with native-image

**Mitigation**:
- Test native-image build early in Phase 2
- Provide reflection configuration for problematic libraries
- Fallback to JVM JAR if native-image fails
- Document known limitations
- Consider alternative libraries if needed

### Risk 5: Large Workflow Runs
**Impact**: Low - Workflows with 100+ jobs may be slow to render

**Mitigation**:
- Pagination for workflow list (default 30 runs)
- Lazy loading of job details (fetch on demand)
- Virtual scrolling for large lists
- Limit initial fetch to recent runs
- Add performance metrics to identify bottlenecks

## Testing Strategy

### Unit Tests
**Coverage Target**: >80%

**Focus Areas**:
- Domain models: Case class construction and validation
- Service layer: Business logic with mocked dependencies
- Formatters: Output formatting for various inputs
- Components: Rendering logic with fixed state
- Filters: Filter application and combination

**Tools**: ScalaTest, ScalaCheck for property-based testing

### Integration Tests
**Focus Areas**:
- API client: Test with WireMock or recorded responses
- CLI commands: End-to-end command execution
- Dashboard: Component integration
- Error scenarios: Network failures, rate limits

**Tools**: ScalaTest, WireMock, TestContainers (if needed)

### Property-Based Tests
**Focus Areas**:
- State transitions: All valid state changes
- Filter logic: Filter combinations
- Pagination: Edge cases
- Duration calculations: Various time ranges

**Tools**: ScalaCheck

### Manual Testing
**Focus Areas**:
- Terminal compatibility: Different terminals and sizes
- Performance: Large workflow lists (100+ runs)
- Error scenarios: Network failures, invalid tokens
- User experience: Keyboard navigation flow
- Visual appearance: Colors, Unicode symbols, layout

## Performance Targets

| Metric | Target | Rationale |
|--------|--------|-----------|
| Dashboard refresh | <1s | Real-time monitoring requires fast updates |
| Native binary startup | <1s | Quick access for developers |
| API response time | <500ms | GitHub API is fast, client should not add overhead |
| Memory usage | <100MB | Lightweight tool for developer machines |
| Terminal render | <100ms | Smooth user experience |

## Success Criteria

### Functional Requirements
- [ ] Dashboard displays workflow runs with all required information
- [ ] All CLI commands work correctly
- [ ] Auto-refresh updates dashboard every N seconds
- [ ] Keyboard navigation is responsive and intuitive
- [ ] Filters work correctly (status, branch, actor)
- [ ] Actions (restart, cancel) execute successfully
- [ ] Watch mode monitors runs until completion

### Non-Functional Requirements
- [ ] Dashboard refreshes in <1 second
- [ ] Native binary starts in <1 second
- [ ] Tests achieve >80% code coverage
- [ ] Zero critical bugs in initial release
- [ ] Documentation is comprehensive and clear
- [ ] Works on macOS, Linux, and Windows (via WSL)

### User Experience
- [ ] Keyboard shortcuts are discoverable
- [ ] Error messages are clear and actionable
- [ ] Colors and symbols enhance readability
- [ ] Layout adapts to terminal size
- [ ] Rate limit warnings prevent surprises

## Future Enhancements (Post v1.0)

### Advanced Features
- Workflow file syntax highlighting in detail view
- Log streaming for running jobs
- Desktop notifications for workflow completion
- Multi-repository dashboard
- Workflow comparison view
- Saved filters and preferences

### Performance Optimizations
- Incremental updates (only fetch changed runs)
- Persistent cache to disk
- Optimized rendering for large lists
- Lazy loading for job details
- Connection pooling optimizations

### User Experience
- Customizable themes
- Saved filters
- Workflow favorites
- Search functionality
- Export to CSV/Excel

### Enterprise Support
- GitHub Enterprise Server URL configuration
- Enterprise authentication options
- Custom API endpoints
- SSO integration

## Getting Started

### Prerequisites
- Scala 3.5.0+
- sbt 1.10.0+
- JDK 21+
- GitHub personal access token with `repo` and `workflow` scopes

### Development Setup

1. **Clone the repository**:
   ```bash
   cd scala/github-actions-cli
   ```

2. **Install dependencies**:
   ```bash
   sbt update
   ```

3. **Set up GitHub token**:
   ```bash
   export GITHUB_TOKEN=ghp_your_token_here
   ```

4. **Run tests**:
   ```bash
   sbt test
   ```

5. **Run the application**:
   ```bash
   sbt "cli/run dashboard"
   ```

### Building

**JAR**:
```bash
sbt assembly
java -jar target/scala-3.5.0/github-actions-cli.jar dashboard
```

**Native Image**:
```bash
sbt nativeImage
./target/native-image/github-actions-cli dashboard
```

## Contributing

See [openspec/changes/add-github-actions-cli/tasks.md](../../openspec/changes/add-github-actions-cli/tasks.md) for detailed implementation tasks.

### Development Workflow
1. Pick a task from tasks.md
2. Create a feature branch
3. Implement with tests
4. Run `sbt test` and `sbt compile`
5. Submit PR with task reference

### Code Style
- Follow Scala 3 best practices
- Use functional programming patterns
- Prefer immutable data structures
- Write comprehensive tests
- Document public APIs

## References

- **GitHub REST API**: https://docs.github.com/en/rest
- **GitHub OpenAPI Spec**: https://github.com/github/rest-api-description
- **Typelevel**: https://typelevel.org/
- **cats-effect**: https://typelevel.org/cats-effect/
- **http4s**: https://http4s.org/
- **decline**: https://ben.kirw.in/decline/

## License

[To be determined]

## Contact

[To be determined]


