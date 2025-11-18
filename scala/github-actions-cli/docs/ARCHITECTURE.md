# Architecture Documentation

Detailed architecture documentation for GitHub Actions CLI.

## Table of Contents

- [Overview](#overview)
- [System Architecture](#system-architecture)
- [Module Architecture](#module-architecture)
- [Data Flow](#data-flow)
- [Effect System](#effect-system)
- [Concurrency Model](#concurrency-model)
- [Error Handling](#error-handling)
- [Testing Strategy](#testing-strategy)

## Overview

GitHub Actions CLI is built using **pure functional programming** principles with the Typelevel stack. The architecture emphasizes:

- **Type Safety**: Leveraging Scala 3's advanced type system
- **Effect Polymorphism**: Using `F[_]` for effect abstraction
- **Resource Safety**: Automatic resource management with `Resource`
- **Composability**: Small, composable functions and components
- **Testability**: Pure functions and dependency injection via algebras

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         CLI Layer                           │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Main.scala - Entry Point (decline-effect)          │  │
│  │  Commands.scala - Command Definitions                │  │
│  │  CommandExecutor.scala - Command Execution           │  │
│  │  Config.scala - Configuration Management             │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      Terminal UI Layer                      │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Dashboard.scala - Main Dashboard Component          │  │
│  │  DashboardState.scala - State Management             │  │
│  │  KeyEvent.scala - Keyboard Input Handling            │  │
│  │  Terminal.scala - Terminal Abstraction               │  │
│  │  Component.scala - UI Components                     │  │
│  │  Style.scala - Styling Utilities                     │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     API Client Layer                        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  GitHubClient.scala - API Algebra                    │  │
│  │  Http4sGitHubClient.scala - HTTP Implementation      │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                       Core Layer                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Models.scala - Domain Models                        │  │
│  │  WorkflowStatus.scala - Status Enum                  │  │
│  │  WorkflowConclusion.scala - Conclusion Enum          │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
                   ┌────────────────┐
                   │  GitHub API    │
                   └────────────────┘
```

## Module Architecture

### Core Module

**Purpose**: Domain models and business logic

**Dependencies**: None (pure domain layer)

**Key Components**:
- `WorkflowRun`, `Job`, `Step` - Domain models
- `WorkflowStatus`, `WorkflowConclusion` - Enums
- `Repository`, `Actor`, `Commit` - Supporting models
- Circe codecs for JSON serialization

**Design Principles**:
- Pure data structures
- No external dependencies
- JSON codec derivation with circe

### API Client Module

**Purpose**: GitHub API integration

**Dependencies**: Core, http4s, circe

**Key Components**:
- `GitHubClient[F[_]]` - Effect algebra
- `Http4sGitHubClient[F[_]]` - HTTP implementation
- `GitHubError` - Error types
- `RateLimit` - Rate limit tracking

**Design Principles**:
- Tagless final pattern
- Effect polymorphism with `F[_]`
- Resource-safe HTTP client
- Automatic rate limit tracking

### Terminal UI Module

**Purpose**: Terminal user interface

**Dependencies**: Core, API Client, fansi

**Key Components**:
- `Terminal[F[_]]` - Terminal algebra
- `Dashboard[F[_]]` - Main dashboard
- `DashboardState` - State management
- `KeyEvent`, `KeyReader[F[_]]` - Input handling
- `Component` - UI component trait
- `Style` - Styling utilities

**Design Principles**:
- Component-based architecture
- Immutable state with `Ref`
- Event-driven with fs2 streams
- Concurrent event loop and auto-refresh

### CLI Module

**Purpose**: Command-line interface

**Dependencies**: Core, API Client, Terminal UI, decline

**Key Components**:
- `Main` - Entry point
- `Command` - Command ADT
- `CommandExecutor[F[_]]` - Command execution
- `CliConfig` - Configuration

**Design Principles**:
- Functional command parsing with decline
- Configuration hierarchy (env > file > defaults)
- Resource-safe execution

## Data Flow

### Dashboard Flow

```
User Input → KeyReader → KeyEvent → NavigationAction
                                          │
                                          ▼
                                    DashboardState
                                          │
                                          ▼
                                    Dashboard.render
                                          │
                                          ▼
                                    Terminal.print
```

### API Request Flow

```
Command → CommandExecutor → GitHubClient → Http4sGitHubClient
                                                    │
                                                    ▼
                                              HTTP Request
                                                    │
                                                    ▼
                                              GitHub API
                                                    │
                                                    ▼
                                              JSON Response
                                                    │
                                                    ▼
                                              Circe Decode
                                                    │
                                                    ▼
                                              Domain Model
```

### Event Loop Flow

```
┌─────────────────────────────────────────────────────────┐
│                     Dashboard.run                       │
│                                                         │
│  ┌──────────────────┐      ┌──────────────────┐       │
│  │  Event Loop      │      │  Auto-Refresh    │       │
│  │  (KeyReader)     │      │  (fs2.Stream)    │       │
│  │                  │      │                  │       │
│  │  Read Key        │      │  Sleep interval  │       │
│  │  ↓               │      │  ↓               │       │
│  │  Parse Event     │      │  Fetch data      │       │
│  │  ↓               │      │  ↓               │       │
│  │  Update State    │      │  Update State    │       │
│  │  ↓               │      │  ↓               │       │
│  │  Render          │      │  Render          │       │
│  │  ↓               │      │  ↓               │       │
│  │  Loop            │      │  Loop            │       │
│  └──────────────────┘      └──────────────────┘       │
│           │                         │                  │
│           └─────────.both───────────┘                  │
│                      │                                 │
│                      ▼                                 │
│              Run concurrently                          │
└─────────────────────────────────────────────────────────┘
```

## Effect System

### Effect Abstraction

All components use effect polymorphism:

```scala
trait GitHubClient[F[_]]:
  def listWorkflowRuns(owner: String, repo: String): F[List[WorkflowRun]]

class Dashboard[F[_]: Async](...)
```

### Type Class Constraints

- `Sync[F]` - Synchronous effects (delay, blocking)
- `Async[F]` - Asynchronous effects (async, cancelation)
- `Concurrent[F]` - Concurrent effects (start, race, both)
- `Console[F]` - Console I/O

### Concrete Effect Type

At the application boundary, `F[_]` is instantiated to `IO`:

```scala
object Main extends CommandIOApp:
  override def main: Opts[IO[ExitCode]] = ???
```

## Concurrency Model

### Dashboard Concurrency

The dashboard runs two concurrent processes:

1. **Event Loop**: Reads keyboard input and updates state
2. **Auto-Refresh**: Periodically fetches fresh data

Both run concurrently using `.both`:

```scala
eventLoop.both(autoRefresh).void
```

### State Management

State is managed with `Ref[F, DashboardState]`:

```scala
for
  stateRef <- Ref.of[F, DashboardState](initialState)
  _ <- eventLoop(stateRef).both(autoRefresh(stateRef))
yield ()
```

### Cancellation

Both processes are cancelable:
- User presses `q` → cancels both processes
- Error occurs → cancels both processes

## Error Handling

### Error Types

```scala
sealed trait GitHubError extends Throwable
  case class Unauthorized(message: String)
  case class NotFound(message: String)
  case class RateLimitExceeded(resetAt: Long)
  case class ServerError(status: Int, message: String)
  case class NetworkError(cause: Throwable)
```

### Error Recovery

```scala
gitHubClient.listWorkflowRuns(owner, repo)
  .handleErrorWith {
    case GitHubError.Unauthorized(msg) =>
      Console[F].println(s"Auth error: $msg") *> F.raiseError(...)
    case err =>
      F.raiseError(err)
  }
```

### User-Facing Errors

Errors are caught at the command executor level and displayed to users:

```scala
executor.execute(cmd).handleErrorWith { err =>
  IO.println(s"Error: ${err.getMessage}") *>
  IO.pure(ExitCode.Error)
}
```

## Testing Strategy

### Unit Tests

Test pure functions and domain logic:

```scala
class WorkflowStatusSpec extends AnyFlatSpec:
  "WorkflowStatus.fromString" should "parse valid status" in {
    WorkflowStatus.fromString("completed") shouldBe Some(WorkflowStatus.Completed)
  }
```

### Effect Tests

Test effectful code with cats-effect-testing:

```scala
class GitHubClientSpec extends AsyncFlatSpec with AsyncIOSpec:
  "GitHubClient" should "list workflow runs" in {
    val client = new TestGitHubClient[IO]
    client.listWorkflowRuns("owner", "repo")
      .asserting(_.length shouldBe 3)
  }
```

### Integration Tests

Test HTTP client with mock server:

```scala
// Use http4s-testing or WireMock
val mockServer = HttpRoutes.of[IO] {
  case GET -> Root / "repos" / owner / repo / "actions" / "runs" =>
    Ok(mockResponse)
}
```

### Property-Based Tests

Test invariants with ScalaCheck:

```scala
property("status roundtrip") {
  forAll { (status: WorkflowStatus) =>
    WorkflowStatus.fromString(status.toString) == Some(status)
  }
}
```

## Design Patterns

### Tagless Final

Define algebras as traits with effect type parameter:

```scala
trait GitHubClient[F[_]]:
  def listWorkflowRuns(...): F[List[WorkflowRun]]
```

### Algebra Pattern

Separate interface from implementation:

```scala
trait Terminal[F[_]]  // Interface
class ConsoleTerminal[F[_]] extends Terminal[F]  // Implementation
```

### Resource Pattern

Safe resource management:

```scala
EmberClientBuilder.default[IO].build.use { client =>
  // client automatically closed
}
```

### State Pattern

Immutable state with `Ref`:

```scala
Ref.of[F, State](initial).flatMap { ref =>
  ref.modify(state => (newState, result))
}
```

## Performance Considerations

### HTTP Client Pooling

http4s EmberClient uses connection pooling automatically.

### Rate Limiting

Client tracks rate limits and stores in `Ref` for efficient access.

### Terminal Rendering

Only re-render when state changes to minimize terminal I/O.

### Streaming

Use fs2 streams for efficient event processing.

## Security Considerations

### Token Storage

- Tokens stored in config file with user-only permissions
- Environment variables preferred for CI/CD
- Never log or display tokens

### HTTPS

All GitHub API requests use HTTPS.

### Input Validation

- Validate user input before API calls
- Sanitize terminal output to prevent injection

---

## Further Reading

- [Typelevel Documentation](https://typelevel.org/)
- [Tagless Final Pattern](https://typelevel.org/blog/2018/05/09/tagless-final-streaming.html)
- [cats-effect Best Practices](https://typelevel.org/cats-effect/docs/best-practices)

