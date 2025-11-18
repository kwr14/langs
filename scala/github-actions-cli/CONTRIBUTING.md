# Contributing to GitHub Actions CLI

Thank you for your interest in contributing to GitHub Actions CLI! This document provides guidelines and instructions for contributing.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Development Workflow](#development-workflow)
- [Testing](#testing)
- [Code Style](#code-style)
- [Submitting Changes](#submitting-changes)

## Code of Conduct

This project follows the [Scala Code of Conduct](https://www.scala-lang.org/conduct/). Please be respectful and constructive in all interactions.

## Getting Started

### Prerequisites

- Java 11 or higher (Java 21 recommended)
- sbt 1.10.0 or higher
- Git
- (Optional) GraalVM for native image builds

### Fork and Clone

1. Fork the repository on GitHub
2. Clone your fork:
   ```bash
   git clone https://github.com/YOUR_USERNAME/langs.git
   cd langs/scala/github-actions-cli
   ```
3. Add upstream remote:
   ```bash
   git remote add upstream https://github.com/kwr14/langs.git
   ```

## Development Setup

### Install Dependencies

All dependencies are managed by sbt and will be downloaded automatically:

```bash
sbt compile
```

### Run Tests

```bash
sbt test
```

### Run the Application

```bash
# Set your GitHub token
export GITHUB_TOKEN=ghp_your_token_here

# Run from source
sbt "cli/run dashboard -o octocat -r Hello-World"

# Or build and run JAR
sbt "cli/assembly"
java -jar cli/target/scala-3.5.0/github-actions-cli.jar --help
```

## Project Structure

```
github-actions-cli/
├── core/           # Domain models and business logic
├── api-client/     # GitHub API client (http4s)
├── terminal-ui/    # Terminal UI components (fansi)
├── cli/            # CLI entry point (decline)
├── project/        # sbt build configuration
├── scripts/        # Installation and utility scripts
└── .github/        # GitHub Actions workflows
```

## Development Workflow

### 1. Create a Branch

```bash
git checkout -b feature/your-feature-name
# or
git checkout -b fix/your-bug-fix
```

### 2. Make Changes

- Write clean, idiomatic Scala 3 code
- Follow functional programming principles
- Use the Typelevel stack conventions
- Add tests for new functionality
- Update documentation as needed

### 3. Test Your Changes

```bash
# Run all tests
sbt test

# Run specific test
sbt "testOnly *YourTestSpec"

# Check formatting
sbt scalafmtCheck

# Format code
sbt scalafmtAll
```

### 4. Commit Changes

Write clear, descriptive commit messages:

```bash
git add .
git commit -m "feat: add support for filtering by actor

- Add actor filter to RunFilter
- Update CLI to accept --actor option
- Add tests for actor filtering"
```

Commit message format:
- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation changes
- `test:` - Test additions or changes
- `refactor:` - Code refactoring
- `chore:` - Build/tooling changes

## Testing

### Writing Tests

- Use ScalaTest for unit tests
- Use ScalaCheck for property-based tests
- Use cats-effect-testing for effect tests
- Aim for >80% code coverage

Example test:

```scala
class MyFeatureSpec extends AnyFlatSpec with Matchers:
  "MyFeature" should "do something" in {
    val result = MyFeature.doSomething()
    result shouldBe expected
  }
```

### Running Tests

```bash
# All tests
sbt test

# Specific module
sbt "core/test"

# Specific test class
sbt "testOnly *GitHubClientSpec"

# With coverage
sbt coverage test coverageReport
```

## Code Style

### Scala Style Guide

- Follow [Scala 3 style guide](https://docs.scala-lang.org/style/)
- Use significant indentation (no braces)
- Prefer `given`/`using` over implicit
- Use enums for ADTs
- Use extension methods for type class syntax

### Formatting

This project uses Scalafmt for code formatting:

```bash
# Check formatting
sbt scalafmtCheck

# Format code
sbt scalafmtAll
```

### Best Practices

- **Pure Functions**: Prefer pure functions over side effects
- **Effect Abstraction**: Use `F[_]` for effect polymorphism
- **Resource Safety**: Use `Resource` for resource management
- **Error Handling**: Use `Either`, `Option`, or effect error handling
- **Type Safety**: Leverage Scala 3's type system
- **Documentation**: Add ScalaDoc for public APIs

## Submitting Changes

### 1. Push to Your Fork

```bash
git push origin feature/your-feature-name
```

### 2. Create Pull Request

1. Go to the original repository on GitHub
2. Click "New Pull Request"
3. Select your fork and branch
4. Fill in the PR template:
   - Description of changes
   - Related issues
   - Testing performed
   - Screenshots (if UI changes)

### 3. Code Review

- Address review comments
- Keep the PR focused and small
- Rebase on main if needed:
  ```bash
  git fetch upstream
  git rebase upstream/main
  git push --force-with-lease
  ```

### 4. Merge

Once approved, a maintainer will merge your PR.

## Additional Resources

- [Scala 3 Documentation](https://docs.scala-lang.org/scala3/)
- [Typelevel Documentation](https://typelevel.org/)
- [cats-effect Documentation](https://typelevel.org/cats-effect/)
- [http4s Documentation](https://http4s.org/)
- [GitHub REST API](https://docs.github.com/en/rest)

## Questions?

Feel free to:
- Open an issue for questions
- Join discussions in GitHub Discussions
- Reach out to maintainers

Thank you for contributing! 🎉

