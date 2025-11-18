# GitHub Actions CLI - Quick Start Guide

## Overview

This guide will help you get started with the GitHub Actions Workflow CLI Dashboard project. This is a **new project** being built from scratch following the OpenSpec methodology.

## Project Status

**Current Phase**: Planning and Specification  
**Implementation Status**: Not started  
**Next Steps**: Begin Phase 1 implementation

## What This Project Will Provide

Once implemented, this tool will offer:

1. **Interactive Terminal Dashboard**
   - Real-time workflow run monitoring
   - Color-coded status indicators
   - Progress bars for running jobs
   - Keyboard-driven navigation

2. **CLI Commands**
   - `gh-actions dashboard` - Interactive dashboard
   - `gh-actions list` - List workflow runs
   - `gh-actions show <run-id>` - Show run details
   - `gh-actions restart <run-id>` - Restart workflows
   - `gh-actions cancel <run-id>` - Cancel workflows
   - `gh-actions watch <run-id>` - Watch until completion

3. **Key Features**
   - Type-safe GitHub API client (OpenAPI-generated)
   - Pure functional architecture (Typelevel stack)
   - Fast native binary (GraalVM)
   - Multiple output formats (table, JSON, YAML)

## Project Structure

```
scala/github-actions-cli/          # Main project (to be created)
├── core/                          # Domain models and business logic
├── api-client/                    # GitHub API integration
├── terminal-ui/                   # TUI components
├── cli/                           # CLI entry point
└── tests/                         # Unit and integration tests

openspec/changes/add-github-actions-cli/  # Specification and planning
├── proposal.md                    # Why and what
├── design.md                      # Technical decisions
├── tasks.md                       # Implementation checklist
└── specs/                         # Requirements
    ├── cli-interface/
    ├── github-api-client/
    ├── terminal-dashboard/
    └── workflow-management/
```

## Understanding the Specifications

### 1. Read the Proposal
Start here: [proposal.md](../../openspec/changes/add-github-actions-cli/proposal.md)

This explains:
- Why we're building this tool
- What capabilities it will have
- Impact on the codebase

### 2. Review the Design
Next: [design.md](../../openspec/changes/add-github-actions-cli/design.md)

This covers:
- Architecture and module structure
- Technology stack choices
- Key design decisions
- Data models
- Risk mitigation strategies

### 3. Check the Tasks
Implementation plan: [tasks.md](../../openspec/changes/add-github-actions-cli/tasks.md)

This provides:
- 7-phase implementation roadmap
- Detailed task breakdown
- Testing strategy
- Success metrics

### 4. Explore the Specifications
Requirements: [specs/](../../openspec/changes/add-github-actions-cli/specs/)

Four capability specifications:
- **cli-interface**: Command-line interface requirements
- **github-api-client**: API integration requirements
- **terminal-dashboard**: Interactive UI requirements
- **workflow-management**: Business logic requirements

Each spec contains:
- Requirements with SHALL/MUST statements
- Scenarios with WHEN/THEN conditions
- Complete behavior specifications

## Implementation Roadmap

### Phase 1: Foundation (Week 1)
- Set up project structure
- Add dependencies
- Create domain models

### Phase 2: API Client (Week 2)
- OpenAPI client generation
- HTTP client implementation
- Error handling and retry logic

### Phase 3: CLI Interface (Week 3)
- Command-line parsing
- Subcommand implementation
- Output formatting

### Phase 4: Terminal Dashboard (Week 4)
- Terminal UI framework
- Component implementation
- Keyboard navigation

### Phase 5: Services (Week 5)
- Workflow service
- Dashboard service
- Caching and state management

### Phase 6: Integration (Week 6)
- Wire components together
- Advanced features
- Comprehensive testing

### Phase 7: Distribution (Week 7)
- Native binary build
- CI/CD setup
- Documentation
- Release v0.1.0

## Technology Stack

### Core
- **Scala 3.5.0**: Modern Scala with enums, union types
- **cats-effect 3.5.4**: Effect system
- **fs2 3.10.2**: Streaming

### HTTP & API
- **http4s 0.23.27**: HTTP client
- **circe 0.14.10**: JSON
- **OpenAPI generator**: Type-safe client

### CLI & Terminal
- **decline 2.4.1**: CLI parsing
- **tui-scala / crossterm-scala**: Terminal UI
- **fansi 0.5.0**: ANSI colors

### Build
- **sbt 1.10.0**: Build tool
- **sbt-native-image**: GraalVM
- **sbt-assembly**: Fat JAR

## Getting Started with Development

### Prerequisites
- Scala 3.5.0+
- sbt 1.10.0+
- JDK 21+
- GitHub personal access token

### Step 1: Understand OpenSpec Workflow

This project follows the OpenSpec methodology:

1. **Specification Phase** (Current)
   - Proposal defines why and what
   - Design documents technical decisions
   - Specs define requirements
   - Tasks provide implementation plan

2. **Implementation Phase** (Next)
   - Follow tasks.md sequentially
   - Implement one phase at a time
   - Write tests alongside code
   - Update task checklist

3. **Archive Phase** (After deployment)
   - Move change to archive
   - Update main specs if needed

### Step 2: Review All Specifications

Before coding, read all specification files:

```bash
# View proposal
cat openspec/changes/add-github-actions-cli/proposal.md

# View design
cat openspec/changes/add-github-actions-cli/design.md

# View tasks
cat openspec/changes/add-github-actions-cli/tasks.md

# View specs
ls openspec/changes/add-github-actions-cli/specs/
```

### Step 3: Validate the Proposal

```bash
# Ensure proposal is well-formed
openspec validate add-github-actions-cli --strict

# View proposal summary
openspec show add-github-actions-cli
```

### Step 4: Start Implementation

When ready to implement:

1. Create project directory:
   ```bash
   mkdir -p scala/github-actions-cli
   ```

2. Follow Phase 1 tasks from tasks.md

3. Check off tasks as you complete them

## Key Concepts

### Effect Algebras
Define interfaces for external dependencies:
```scala
trait GitHubClient[F[_]]:
  def listWorkflowRuns(...): F[WorkflowRunsResponse]

trait Terminal[F[_]]:
  def render(screen: Screen): F[Unit]
```

### Component-Based UI
Compose dashboard from reusable components:
```scala
trait Component[F[_]]:
  def render(state: State, bounds: Rect): F[RenderTree]
```

### State Management
Use Ref for thread-safe mutable state:
```scala
case class DashboardState(
  runs: List[WorkflowRun],
  selectedIndex: Int,
  filter: RunFilter
)
```

## Next Steps

1. **Review all specification documents** in `openspec/changes/add-github-actions-cli/`
2. **Understand the architecture** from design.md
3. **Familiarize with the task breakdown** in tasks.md
4. **Start Phase 1 implementation** when ready
5. **Follow OpenSpec workflow** for changes and updates

## Questions?

- Check the [PROJECT_PLAN.md](./PROJECT_PLAN.md) for comprehensive details
- Review OpenSpec methodology in [openspec/AGENTS.md](../../openspec/AGENTS.md)
- Consult the specification files for requirements

## Resources

- **GitHub REST API Docs**: https://docs.github.com/en/rest
- **Typelevel**: https://typelevel.org/
- **cats-effect Guide**: https://typelevel.org/cats-effect/docs/getting-started
- **http4s Tutorial**: https://http4s.org/v0.23/docs/quickstart.html
- **decline Documentation**: https://ben.kirw.in/decline/

