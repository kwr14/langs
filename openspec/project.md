# Project Context

## Purpose
A polyglot monorepo for language experimentation and learning across multiple programming paradigms. The repository serves as a playground for prototyping ideas, exploring functional programming concepts, testing new language features, and experimenting with distributed systems patterns.

## Tech Stack

### Languages & Runtimes
- **Python 3.14** - Modern Python with `uv` package manager
- **Scala 2.13 & 3.5** - Functional programming with sbt build tool
- **Gleam 1.13.0** - Type-safe functional language on BEAM (Erlang/OTP 27.1)
- **Java** - Continuations and advanced JVM features exploration
- **LLM/RAG** - Retrieval-augmented generation experiments

### Frameworks & Libraries

#### Scala Stack
- **Cats Effect 3.5+** - Functional effects and concurrency
- **http4s 0.23** - Functional HTTP server/client (Ember backend)
- **FS2 3.10** - Functional streams
- **Kyo 0.11** - Algebraic effects library
- **Cassandra Driver 4.17** - DataStax Java driver
- **jsoniter-scala 2.30** - High-performance JSON codec
- **ScalaSQL 0.1** - Type-safe SQL DSL
- **ScalaTest & MUnit** - Testing frameworks

#### Python Stack
- **uv** - Fast Python package manager
- **Hypothesis 6.0+** - Property-based testing

#### Gleam Stack
- **gleam_stdlib 0.65** - Standard library
- **gleeunit 1.9** - Testing framework

### Infrastructure & Tooling
- **GitHub Actions** - CI/CD automation
- **Docker & Docker Compose** - Cassandra, Kafka, Zookeeper, Schema Registry
- **sbt 1.9.8** - Scala build tool
- **Git hooks** - Pre-push formatting (`.githooks/`)
- **act** - Local CI testing with Docker
- **OpenSpec** - Spec-driven development workflow

## Project Conventions

### Code Style

#### Scala
- Scala 2.13 for production code, Scala 3.5 for experiments
- Functional programming style with Cats Effect
- Prefer immutable data structures
- Use for-comprehensions for sequential effects
- Type-safe error handling (Either, IO, etc.)
- Naming: camelCase for methods/variables, PascalCase for types

#### Python
- Python 3.14 with type hints
- PEP 8 style guide
- Use `uv` for dependency management
- Property-based testing with Hypothesis
- Docstrings for public functions

#### Gleam
- Automatic formatting with `gleam format`
- Pre-push hook enforces formatting
- Functional style with pattern matching
- Type-safe by default

### Architecture Patterns

#### Monorepo Structure
- Top-level directories per language: `python/`, `scala/`, `gleam/`, `java/`, `llm-rag/`
- Each language may contain multiple subprojects
- Shared CI workflows in `.github/workflows/`
- Path-based CI triggers (only run CI for changed modules)

#### Scala Projects
- **effects/** - Kyo algebraic effects, trampolines, functional composition
- **durabletask/** - Durable task execution patterns with SQLite persistence
- **cassandra-best-practise/** - Cassandra integration, Kafka streaming, workflow orchestration
  - Multi-module sbt project (root + core)
  - http4s REST API for workflow management
  - Durable execution client/worker pattern
  - In-memory and persistent storage layers
- **friday-talk/** - Continuations and advanced topics
- **kafka4s/** - Kafka integration experiments

#### Key Patterns
- **Tagless Final** - Abstract over effect types
- **Durable Execution** - Workflow orchestration with persistence
- **Streaming** - FS2 for data pipelines
- **Client/Worker** - Distributed task execution
- **Repository Pattern** - Persistence abstraction

### Testing Strategy

#### Scala
- Unit tests with ScalaTest or MUnit
- Integration tests for Cassandra/Kafka (Docker Compose)
- CI runs all tests on push to relevant paths

#### Python
- Unit tests with unittest
- Property-based tests with Hypothesis
- CI runs tests with `uv run python -m unittest`

#### Gleam
- Unit tests with gleeunit
- Format checking in CI (`gleam format --check`)
- Pre-push hook auto-formats code

#### CI Strategy
- Path-based triggers (only run CI for changed files)
- Consolidated CI status in `CI_STATUS.md`
- Build monitor workflow generates status page
- GitHub Pages deployment at https://kwr14.github.io/langs/
- Multi-repository monitoring (langs + phel-lang)

### Git Workflow

#### Branching
- `main` branch for stable code
- Feature branches for experiments
- Direct commits to main for small changes

#### Commit Conventions
- Conventional commits style (optional)
- Descriptive commit messages
- Examples: "Feature: Add X", "Fix: Y", "Refactor: Z", "Test: W"

#### Pre-push Hooks
- Located in `.githooks/` (configured with `git config core.hooksPath .githooks`)
- Auto-format Gleam code before push
- Can bypass with `git push --no-verify`

#### CI/CD
- GitHub Actions for all CI
- Manual workflow dispatch available
- Build monitor runs hourly + on-demand
- Deploys status page to GitHub Pages

## Domain Context

### Functional Programming
- Heavy focus on functional effects (Cats Effect, Kyo)
- Algebraic effects and handlers
- Trampolines and tail recursion
- Monad transformers and composition

### Distributed Systems
- Durable task execution patterns
- Workflow orchestration
- Event streaming with Kafka
- Cassandra for distributed storage
- Client/worker architecture

### Language Exploration
- Comparing functional approaches across languages
- Type systems (Scala, Gleam)
- Effect systems (Cats Effect, Kyo, Gleam)
- Continuations (Java, Scala)

## Important Constraints

### Technical
- Scala projects must support both 2.13 and 3.5 where applicable
- Python requires 3.14 (pre-release)
- Gleam requires Erlang/OTP 27.1
- Docker required for Cassandra/Kafka integration tests
- GitHub Actions free tier limits

### Development
- Keep CI green - all tests must pass
- Path-based CI to avoid unnecessary builds
- Prefer small, focused changes
- Document experiments in README files

### Infrastructure
- Public repository (required for GitHub Pages)
- No sensitive data in commits
- Docker Compose for local development
- act for local CI testing

## External Dependencies

### Services
- **GitHub Actions** - CI/CD platform
- **GitHub Pages** - Status page hosting (https://kwr14.github.io/langs/)
- **Docker Hub** - Container images (Cassandra, Kafka, etc.)
- **Hex.pm** - Gleam package registry
- **PyPI** - Python package registry
- **Maven Central** - Scala/Java dependencies
- **Confluent** - Kafka packages

### APIs
- **GitHub REST API** - CI status monitoring, workflow runs
- **GitHub GraphQL API** - (potential future use)

### Development Tools
- **GitHub CLI (gh)** - Workflow management
- **uv** - Python package manager
- **sbt** - Scala build tool
- **gleam** - Gleam compiler and tooling
- **act** - Local CI runner
- **OpenSpec** - Spec-driven development

### Monitoring
- CI Build Monitor tracks workflows across multiple repositories
- Displays status, conclusion, last run time, and failure details
- Auto-refreshes every 5 minutes on GitHub Pages
- Supports creating issues and assigning to commit authors
