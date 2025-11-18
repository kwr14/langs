# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial release preparation
- Packaging and distribution infrastructure

## [0.1.0] - TBD

### Added
- Interactive terminal dashboard with real-time workflow monitoring
- GitHub API client with http4s and circe
- CLI commands: `dashboard`, `list`, `show`, `rerun`, `cancel`, `init`, `version`
- Configuration management via file (`~/.github-actions-cli.conf`) and environment variables
- Auto-refresh functionality for dashboard
- Keyboard navigation with vim-style bindings (hjkl)
- Color-coded status indicators for workflows, jobs, and steps
- Progress bars for running jobs
- Filter workflow runs by status, branch, and limit
- Rerun workflows or failed jobs only
- Cancel running workflows
- Multiple output formats (table, JSON, plain text) - planned
- Fat JAR packaging with sbt-assembly
- GraalVM native image support
- Docker containerization
- Installation scripts for Unix/Linux/macOS
- Homebrew formula (planned)
- CI/CD pipeline with GitHub Actions
- Comprehensive test suite (30+ tests)

### Technical Details
- Built with Scala 3.5.0
- Typelevel stack: cats-effect 3.5.4, http4s 0.23.27, fs2 3.10.2
- JSON parsing with circe 0.14.10
- CLI parsing with decline-effect 2.4.1
- Terminal UI with fansi 0.5.0
- Multi-module sbt project structure
- Pure functional architecture
- Effect-polymorphic design with F[_]

### Known Issues
- Native image build requires GraalVM (not included in standard JDK)
- Output formatting for `list` and `show` commands uses placeholder implementation
- No ARM64 native binaries yet (planned for future release)

## [0.0.1] - 2025-11-18

### Added
- Project setup and foundation
- Core domain models (WorkflowRun, Job, Step, etc.)
- GitHub API client implementation
- Terminal UI components
- Interactive dashboard
- CLI command infrastructure

---

## Release Notes

### v0.1.0 (Upcoming)

This is the first beta release of GitHub Actions CLI. It provides a fully functional terminal-based interface for monitoring and managing GitHub Actions workflows.

**Highlights:**
- 🎨 Interactive dashboard with real-time updates
- ⚡ Fast startup with native binary option
- 🔒 Type-safe GitHub API integration
- ⌨️ Keyboard-driven workflow management
- 🧩 Pure functional architecture

**Installation:**
```bash
# Quick install
curl -fsSL https://raw.githubusercontent.com/kwr14/langs/main/scala/github-actions-cli/scripts/install.sh | bash

# Or download from releases
# https://github.com/kwr14/langs/releases
```

**Getting Started:**
```bash
# Initialize configuration
gh-actions init

# Add your GitHub token to ~/.github-actions-cli.conf
# Or set GITHUB_TOKEN environment variable

# Launch dashboard
gh-actions dashboard -o <owner> -r <repo>
```

**Feedback:**
Please report issues and feature requests at: https://github.com/kwr14/langs/issues

---

[Unreleased]: https://github.com/kwr14/langs/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/kwr14/langs/releases/tag/v0.1.0
[0.0.1]: https://github.com/kwr14/langs/releases/tag/v0.0.1

