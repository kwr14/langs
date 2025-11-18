# GitHub Actions CLI - API Documentation

Developer documentation for the GitHub Actions CLI codebase.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Module Structure](#module-structure)
- [Core Module](#core-module)
- [API Client Module](#api-client-module)
- [Terminal UI Module](#terminal-ui-module)
- [CLI Module](#cli-module)
- [Effect System](#effect-system)
- [Testing](#testing)

## Architecture Overview

The GitHub Actions CLI follows a **pure functional architecture** using the Typelevel stack:

- **Effect System**: cats-effect for IO and concurrency
- **HTTP Client**: http4s for GitHub API communication
- **JSON Parsing**: circe for JSON encoding/decoding
- **Streaming**: fs2 for functional streams
- **CLI Parsing**: decline for command-line argument parsing
- **Terminal UI**: fansi for ANSI terminal formatting

### Design Principles

1. **Effect Polymorphism**: Use `F[_]` for effect abstraction
2. **Tagless Final**: Define algebras as traits with effect type parameter
3. **Resource Safety**: Use `Resource` for resource management
4. **Pure Functions**: Separate pure logic from effects
5. **Type Safety**: Leverage Scala 3's type system

## Module Structure

```
github-actions-cli/
├── core/           # Domain models and business logic
├── api-client/     # GitHub API client
├── terminal-ui/    # Terminal UI components
└── cli/            # CLI entry point
```

### Dependencies

```
cli → terminal-ui → core
cli → api-client → core
```

## Core Module

**Package**: `com.github.actions.domain`

### Domain Models

#### WorkflowRun

Represents a GitHub Actions workflow run.

```scala
case class WorkflowRun(
  id: Long,
  name: String,
  head_branch: String,
  status: WorkflowStatus,
  conclusion: Option[WorkflowConclusion],
  created_at: String,
  updated_at: String,
  run_started_at: Option[String],
  jobs_url: String,
  repository: Repository,
  head_commit: Commit,
  actor: Actor
)
```

**Fields:**
- `id` - Unique workflow run identifier
- `name` - Workflow name
- `head_branch` - Branch name
- `status` - Current status (queued, in_progress, completed)
- `conclusion` - Final result (success, failure, cancelled, etc.)
- `created_at` - ISO 8601 timestamp
- `jobs_url` - URL to fetch jobs
- `repository` - Repository information
- `actor` - User who triggered the workflow

#### WorkflowStatus

Enum representing workflow run status.

```scala
enum WorkflowStatus:
  case Queued
  case InProgress
  case Completed
```

**Methods:**
- `fromString(s: String): Option[WorkflowStatus]` - Parse from string
- `toString: String` - Convert to string

#### WorkflowConclusion

Enum representing workflow run conclusion.

```scala
enum WorkflowConclusion:
  case Success
  case Failure
  case Cancelled
  case Skipped
  case TimedOut
  case ActionRequired
  case Neutral
  case Stale
```

**Methods:**
- `fromString(s: String): Option[WorkflowConclusion]` - Parse from string
- `toString: String` - Convert to string

#### Job

Represents a job within a workflow run.

```scala
case class Job(
  id: Long,
  run_id: Long,
  name: String,
  status: WorkflowStatus,
  conclusion: Option[WorkflowConclusion],
  started_at: Option[String],
  completed_at: Option[String],
  steps: List[Step]
)
```

#### Step

Represents a step within a job.

```scala
case class Step(
  name: String,
  status: WorkflowStatus,
  conclusion: Option[WorkflowConclusion],
  number: Int,
  started_at: Option[String],
  completed_at: Option[String]
)
```

### Circe Codecs

All domain models have automatic JSON codecs:

```scala
import io.circe.generic.auto.*
import io.circe.syntax.*

val run: WorkflowRun = ???
val json = run.asJson
val parsed = json.as[WorkflowRun]
```

## API Client Module

**Package**: `com.github.actions.client`

### GitHubClient Algebra

Effect-based interface for GitHub API operations.

```scala
trait GitHubClient[F[_]]:
  def listWorkflowRuns(
    owner: String,
    repo: String,
    filter: Option[RunFilter] = None
  ): F[List[WorkflowRun]]
  
  def getWorkflowRun(
    owner: String,
    repo: String,
    runId: Long
  ): F[WorkflowRun]
  
  def listWorkflowRunJobs(
    owner: String,
    repo: String,
    runId: Long
  ): F[List[Job]]
  
  def rerunWorkflow(
    owner: String,
    repo: String,
    runId: Long
  ): F[Unit]
  
  def rerunFailedJobs(
    owner: String,
    repo: String,
    runId: Long
  ): F[Unit]
  
  def cancelWorkflowRun(
    owner: String,
    repo: String,
    runId: Long
  ): F[Unit]
  
  def getRateLimit: F[Option[RateLimit]]
```

### Http4sGitHubClient

HTTP4s-based implementation of GitHubClient.

```scala
class Http4sGitHubClient[F[_]: Async: Console](
  client: Client[F],
  config: GitHubClient.Config,
  rateLimitRef: Ref[F, Option[GitHubClient.RateLimit]]
) extends GitHubClient[F]
```

**Configuration:**

```scala
case class Config(
  token: String,
  baseUri: Uri,
  userAgent: String
)
```

**Usage:**

```scala
import cats.effect.IO
import org.http4s.ember.client.EmberClientBuilder

EmberClientBuilder.default[IO].build.use { client =>
  val config = GitHubClient.Config(
    token = "ghp_...",
    baseUri = uri"https://api.github.com",
    userAgent = "github-actions-cli/0.1.0"
  )
  
  for
    rateLimitRef <- Ref.of[IO, Option[GitHubClient.RateLimit]](None)
    gitHubClient = new Http4sGitHubClient[IO](client, config, rateLimitRef)
    runs <- gitHubClient.listWorkflowRuns("octocat", "Hello-World")
  yield runs
}
```

### Error Handling

```scala
sealed trait GitHubError extends Throwable

object GitHubError:
  case class Unauthorized(message: String) extends GitHubError
  case class NotFound(message: String) extends GitHubError
  case class RateLimitExceeded(resetAt: Long) extends GitHubError
  case class ServerError(status: Int, message: String) extends GitHubError
  case class NetworkError(cause: Throwable) extends GitHubError
```

**Handling Errors:**

```scala
gitHubClient.listWorkflowRuns("owner", "repo")
  .handleErrorWith {
    case GitHubError.Unauthorized(msg) =>
      IO.println(s"Auth error: $msg") *> IO.raiseError(???)
    case GitHubError.RateLimitExceeded(resetAt) =>
      IO.println(s"Rate limit exceeded, resets at $resetAt") *> IO.raiseError(???)
    case err =>
      IO.raiseError(err)
  }
```

### Rate Limiting

The client tracks rate limits from GitHub API responses:

```scala
case class RateLimit(
  limit: Int,
  remaining: Int,
  reset: Long
)
```

**Check Rate Limit:**

```scala
for
  rateLimit <- gitHubClient.getRateLimit
  _ <- rateLimit match
    case Some(rl) => IO.println(s"Remaining: ${rl.remaining}/${rl.limit}")
    case None => IO.println("No rate limit info")
yield ()
```

## Terminal UI Module

**Package**: `com.github.actions.ui`

### Terminal Algebra

Effect-based interface for terminal operations.

```scala
trait Terminal[F[_]]:
  def clear: F[Unit]
  def size: F[(Int, Int)]
  def moveCursor(x: Int, y: Int): F[Unit]
  def hideCursor: F[Unit]
  def showCursor: F[Unit]
  def print(s: String): F[Unit]
  def println(s: String): F[Unit]
  def enterAlternateScreen: F[Unit]
  def exitAlternateScreen: F[Unit]
```

**Factory:**

```scala
val terminal = Terminal.console[IO]
```

### Component Trait

Base trait for UI components.

```scala
trait Component:
  def render(width: Int, height: Int): String
```

### Built-in Components

#### Text

Simple text component.

```scala
case class Text(content: String) extends Component
```

#### StyledText

Text with ANSI styling.

```scala
case class StyledText(content: String, style: fansi.EscapeAttr) extends Component
```

#### ItemList

Scrollable list of items.

```scala
case class ItemList[A](
  items: List[A],
  selectedIndex: Int,
  renderItem: (A, Boolean) => String
) extends Component
```

#### ProgressBar

Progress bar component.

```scala
case class ProgressBar(
  current: Int,
  total: Int,
  width: Int,
  showPercentage: Boolean = true
) extends Component
```

### Style Utilities

```scala
object Style:
  // Colors
  val success: fansi.EscapeAttr = fansi.Color.Green
  val failure: fansi.EscapeAttr = fansi.Color.Red
  val warning: fansi.EscapeAttr = fansi.Color.Yellow
  val info: fansi.EscapeAttr = fansi.Color.Blue
  val muted: fansi.EscapeAttr = fansi.Color.LightGray
  
  // Status colors
  def statusColor(status: WorkflowStatus): fansi.EscapeAttr
  def conclusionColor(conclusion: WorkflowConclusion): fansi.EscapeAttr
```

### Dashboard

Main dashboard component with state management.

```scala
class Dashboard[F[_]: Async](
  client: GitHubClient[F],
  terminal: Terminal[F],
  keyReader: KeyReader[F],
  owner: String,
  repo: String
)
```

**Methods:**

```scala
def run(refreshInterval: Option[FiniteDuration]): F[Unit]
```

**Usage:**

```scala
for
  dashboard <- Dashboard[IO](gitHubClient, terminal, keyReader, "owner", "repo")
  _ <- dashboard.run(Some(30.seconds))
yield ()
```

## CLI Module

**Package**: `com.github.actions.cli`

### Commands

Sealed trait representing all CLI commands.

```scala
sealed trait Command

object Command:
  case class Dashboard(owner: String, repo: String, autoRefresh: Boolean, refreshInterval: Int)
  case class List(owner: String, repo: String, status: Option[WorkflowStatus], branch: Option[String], limit: Int)
  case class Show(owner: String, repo: String, runId: Long)
  case class Rerun(owner: String, repo: String, runId: Long, failedOnly: Boolean)
  case class Cancel(owner: String, repo: String, runId: Long)
  case object Init
  case object Version
```

### Configuration

```scala
case class CliConfig(
  githubToken: String,
  defaultOwner: Option[String],
  defaultRepo: Option[String],
  autoRefreshInterval: Int,
  apiBaseUrl: String
)

object CliConfig:
  def load[F[_]: Sync]: F[Option[CliConfig]]
  def createSampleConfig[F[_]: Sync]: F[Unit]
```

### Command Executor

```scala
class CommandExecutor[F[_]: Async: Console](config: CliConfig):
  def execute(cmd: Command): F[Unit]
```

## Effect System

### Effect Polymorphism

All components use effect polymorphism with `F[_]`:

```scala
def myFunction[F[_]: Async](param: String): F[Result] =
  for
    _ <- Async[F].delay(println(s"Processing: $param"))
    result <- Async[F].pure(Result(...))
  yield result
```

### Common Type Classes

- `Sync[F]` - Synchronous effects
- `Async[F]` - Asynchronous effects
- `Concurrent[F]` - Concurrent effects
- `Console[F]` - Console I/O

### Resource Management

Use `Resource` for safe resource handling:

```scala
EmberClientBuilder.default[IO].build.use { client =>
  // client is automatically closed
  gitHubClient.listWorkflowRuns("owner", "repo")
}
```

## Testing

### Unit Tests

Use ScalaTest with cats-effect-testing:

```scala
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

class MySpec extends AsyncFlatSpec with AsyncIOSpec with Matchers:
  "MyComponent" should "do something" in {
    val result = MyComponent.doSomething[IO]()
    result.asserting(_ shouldBe expected)
  }
```

### Mocking

Create test implementations of algebras:

```scala
class TestGitHubClient[F[_]: Applicative] extends GitHubClient[F]:
  def listWorkflowRuns(owner: String, repo: String, filter: Option[RunFilter]) =
    List(
      WorkflowRun(...)
    ).pure[F]
  
  // ... other methods
```

### Property-Based Testing

Use ScalaCheck for property-based tests:

```scala
import org.scalacheck.Prop.forAll

property("WorkflowStatus roundtrip") {
  forAll { (status: WorkflowStatus) =>
    WorkflowStatus.fromString(status.toString) == Some(status)
  }
}
```

---

## Additional Resources

- [Cats Effect Documentation](https://typelevel.org/cats-effect/)
- [http4s Documentation](https://http4s.org/)
- [Circe Documentation](https://circe.github.io/circe/)
- [Decline Documentation](https://ben.kirw.in/decline/)
- [GitHub REST API](https://docs.github.com/en/rest)

## Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md) for development guidelines.

