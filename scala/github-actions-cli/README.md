# GitHub Actions Workflow CLI Dashboard

> A terminal-based GitHub Actions workflow monitoring and management tool built with Scala 3 and the Typelevel stack.

[![Project Status](https://img.shields.io/badge/status-beta-green)]()
[![Scala Version](https://img.shields.io/badge/scala-3.5.0-red)]()
[![License](https://img.shields.io/badge/license-MIT-blue)]()

## 🎯 Project Vision

Monitor and manage GitHub Actions workflows without leaving your terminal. Get real-time updates, drill down into failures, and take action—all with a fast, keyboard-driven interface.

### Features

- ⚡ **Real-time interactive dashboard** with auto-refresh
- 🔒 **Type-safe GitHub API integration** with http4s
- ⌨️ **Keyboard-driven workflow management** (list, view, rerun, cancel)
- 🚀 **Fast native binary** with GraalVM (optional)
- 🧩 **Pure functional architecture** built with Typelevel stack
- 📊 **Multiple output formats** (table, JSON, plain text)
- ⚙️ **Flexible configuration** via file or environment variables

## 📊 Dashboard Preview

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

## 🚀 Quick Start

### Prerequisites

- Java 11+ (for JAR version)
- GitHub personal access token with `repo` and `workflow` scopes

### Installation

#### Quick Install (Unix/Linux/macOS)

```bash
curl -fsSL https://raw.githubusercontent.com/kwr14/langs/main/scala/github-actions-cli/scripts/install.sh | bash
```

#### Manual Installation

**Option 1: Native Binary (Recommended)**

Download from [releases page](https://github.com/kwr14/langs/releases):

```bash
# Linux
curl -L -o gh-actions https://github.com/kwr14/langs/releases/latest/download/gh-actions-linux-x86_64
chmod +x gh-actions
sudo mv gh-actions /usr/local/bin/

# macOS
curl -L -o gh-actions https://github.com/kwr14/langs/releases/latest/download/gh-actions-macos-x86_64
chmod +x gh-actions
sudo mv gh-actions /usr/local/bin/
```

**Option 2: JAR (Requires Java 11+)**

```bash
curl -L -o github-actions-cli.jar https://github.com/kwr14/langs/releases/latest/download/github-actions-cli.jar
java -jar github-actions-cli.jar --help
```

**Option 3: Build from Source**

```bash
git clone https://github.com/kwr14/langs.git
cd langs/scala/github-actions-cli
sbt "cli/assembly"
# JAR: cli/target/scala-3.5.0/github-actions-cli.jar

# Or build native image (requires GraalVM)
sbt "cli/nativeImage"
# Binary: cli/target/native-image/gh-actions
```

### Configuration

**1. Initialize configuration:**

```bash
gh-actions init
```

**2. Add your GitHub token to `~/.github-actions-cli.conf`:**

```properties
github.token=ghp_your_token_here
```

Or set via environment variable:

```bash
export GITHUB_TOKEN=ghp_your_token_here
```

### Usage

**Interactive dashboard:**

```bash
gh-actions dashboard -o <owner> -r <repo>
```

**List workflow runs:**

```bash
gh-actions list -o <owner> -r <repo> --status completed --branch main
```

**Show run details:**

```bash
gh-actions show -o <owner> -r <repo> <run-id>
```

**Rerun workflow:**

```bash
gh-actions rerun -o <owner> -r <repo> <run-id>
gh-actions rerun -o <owner> -r <repo> <run-id> --failed-only
```

**Cancel workflow:**

```bash
gh-actions cancel -o <owner> -r <repo> <run-id>
```

## 📚 Documentation

### User Documentation
- **[Quick Start Guide](docs/QUICKSTART.md)** - Get up and running in 5 minutes
- **[User Guide](docs/USER_GUIDE.md)** - Complete usage guide with examples
- **[Troubleshooting](docs/USER_GUIDE.md#troubleshooting)** - Common issues and solutions

### Developer Documentation
- **[API Documentation](docs/API.md)** - Developer API reference
- **[Architecture](docs/ARCHITECTURE.md)** - System architecture and design
- **[Contributing Guide](CONTRIBUTING.md)** - How to contribute
- **[Release Checklist](docs/RELEASE_CHECKLIST.md)** - Release process

### Project Planning
- **[Changelog](CHANGELOG.md)** - Version history and release notes
- **[Project Plan](./PROJECT_PLAN.md)** - Implementation roadmap
- **[OpenSpec Proposal](../../openspec/changes/add-github-actions-cli/proposal.md)** - Original proposal
- **[Tasks](../../openspec/changes/add-github-actions-cli/tasks.md)** - Implementation checklist

## 🏗️ Architecture

### Module Structure

```
github-actions-cli/
├── core/                    # Domain models and business logic
│   ├── domain/             # WorkflowRun, Job, Step models
│   ├── service/            # WorkflowService, DashboardService
│   └── algebra/            # Effect interfaces
├── api-client/             # GitHub API integration
│   ├── generated/          # OpenAPI-generated code
│   ├── client/             # Http4s client
│   └── auth/               # Token authentication
├── terminal-ui/            # TUI components
│   ├── components/         # Dashboard, WorkflowList, JobProgress
│   ├── renderer/           # Terminal rendering
│   └── input/              # Keyboard events
├── cli/                    # CLI entry point
│   ├── commands/           # Subcommands
│   └── Main.scala          # Application entry
└── tests/
    ├── unit/               # Pure logic tests
    └── integration/        # API client tests
```

### Technology Stack

| Category | Libraries |
|----------|-----------|
| **Core** | Scala 3.5, cats-effect 3.5.4, fs2 3.10.2 |
| **HTTP & API** | http4s 0.23.27, circe 0.14.10, OpenAPI generator |
| **CLI & Terminal** | decline 2.4.1, tui-scala/crossterm-scala, fansi 0.5.0 |
| **Build** | sbt 1.10.0, sbt-native-image, sbt-assembly |

## 🎯 Features

### Interactive Dashboard
- ✅ Real-time auto-refresh (configurable interval)
- ✅ Color-coded status indicators
- ✅ Progress bars for running jobs
- ✅ Keyboard navigation
- ✅ Filter by status, branch, actor
- ✅ Detail view for runs, jobs, and steps

### CLI Commands
- ✅ List workflow runs with filters
- ✅ Show detailed run information
- ✅ Restart workflows or failed jobs
- ✅ Cancel running workflows
- ✅ Watch mode for monitoring
- ✅ Multiple output formats (table, JSON, YAML)

### Developer Experience
- ✅ Type-safe API client (OpenAPI-generated)
- ✅ Pure functional architecture
- ✅ Fast native binary (<1s startup)
- ✅ Auto-detect repository from git
- ✅ Comprehensive error handling
- ✅ Rate limit tracking and warnings

## 📅 Implementation Status

| Phase | Focus | Status |
|-------|-------|--------|
| **Phase 1** | Project setup, domain models | ✅ Complete |
| **Phase 2** | GitHub API client | ✅ Complete |
| **Phase 3** | Terminal UI components | ✅ Complete |
| **Phase 4** | Interactive dashboard | ✅ Complete |
| **Phase 5** | CLI commands | ✅ Complete |
| **Phase 6** | Packaging & distribution | 🚧 In Progress |
| **Phase 7** | Documentation & release | 📋 Planned |

See [tasks.md](../../openspec/changes/add-github-actions-cli/tasks.md) for detailed breakdown.

## 🧪 Testing

- **Unit Tests**: Domain models, services, formatters
- **Integration Tests**: API client, CLI commands
- **Property-Based Tests**: State transitions, filters
- **Manual Tests**: Terminal compatibility, UX

**Coverage Target**: >80%

## 🤝 Contributing

This project follows the OpenSpec methodology for spec-driven development.

1. Review specifications in `openspec/changes/add-github-actions-cli/`
2. Pick a task from `tasks.md`
3. Implement with tests
4. Submit PR with task reference

See [QUICKSTART.md](./QUICKSTART.md) for development setup.

## 📖 Resources

- [GitHub REST API](https://docs.github.com/en/rest)
- [GitHub OpenAPI Spec](https://github.com/github/rest-api-description)
- [Typelevel](https://typelevel.org/)
- [cats-effect](https://typelevel.org/cats-effect/)
- [http4s](https://http4s.org/)
- [decline](https://ben.kirw.in/decline/)

## 📄 License

[To be determined]

## 👥 Authors

[To be determined]

---

**Project Status**: 🚧 Beta (Phase 6 in progress)
**Next Milestone**: Complete packaging and distribution
**Target Release**: v0.1.0

