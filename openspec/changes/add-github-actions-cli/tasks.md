# Implementation Tasks

## Phase 1: Project Setup and Foundation (Week 1)

### 1.1 Project Scaffolding
- [ ] 1.1.1 Create `scala/github-actions-cli/` directory structure
- [ ] 1.1.2 Create `build.sbt` with Scala 3.5.0 and dependencies
- [ ] 1.1.3 Set up sbt project structure (core, api-client, terminal-ui, cli modules)
- [ ] 1.1.4 Create `project/build.properties` with sbt 1.10.0
- [ ] 1.1.5 Create `project/plugins.sbt` with required plugins
- [ ] 1.1.6 Add `.gitignore` for Scala/sbt artifacts
- [ ] 1.1.7 Create README.md with project overview

### 1.2 Core Dependencies
- [ ] 1.2.1 Add cats-effect 3.5.4
- [ ] 1.2.2 Add http4s-ember-client 0.23.27
- [ ] 1.2.3 Add circe 0.14.10 (core, generic, parser)
- [ ] 1.2.4 Add decline 2.4.1
- [ ] 1.2.5 Add fs2 3.10.2
- [ ] 1.2.6 Add fansi 0.5.0 for ANSI colors
- [ ] 1.2.7 Add scalatest 3.3.0-alpha.1 for testing

### 1.3 Domain Models
- [ ] 1.3.1 Create `core/domain/WorkflowStatus.scala` enum
- [ ] 1.3.2 Create `core/domain/WorkflowConclusion.scala` enum
- [ ] 1.3.3 Create `core/domain/WorkflowRun.scala` case class
- [ ] 1.3.4 Create `core/domain/Job.scala` case class
- [ ] 1.3.5 Create `core/domain/Step.scala` case class
- [ ] 1.3.6 Create `core/domain/Actor.scala` case class
- [ ] 1.3.7 Create `core/domain/Repository.scala` case class

## Phase 2: GitHub API Client (Week 2)

### 2.1 OpenAPI Setup
- [ ] 2.1.1 Download GitHub OpenAPI specification
- [ ] 2.1.2 Evaluate OpenAPI generator options (guardrail, openapi-generator, smithy4s)
- [ ] 2.1.3 Configure chosen generator in build.sbt
- [ ] 2.1.4 Generate initial client code
- [ ] 2.1.5 Review and test generated code

### 2.2 HTTP Client Implementation
- [ ] 2.2.1 Create `api-client/client/GitHubClient.scala` trait
- [ ] 2.2.2 Implement `api-client/client/GitHubClientImpl.scala` with http4s
- [ ] 2.2.3 Create `api-client/auth/TokenAuth.scala` for authentication
- [ ] 2.2.4 Implement request/response logging
- [ ] 2.2.5 Add User-Agent header configuration

### 2.3 API Endpoints
- [ ] 2.3.1 Implement `listWorkflowRuns` method
- [ ] 2.3.2 Implement `getWorkflowRun` method
- [ ] 2.3.3 Implement `listWorkflowRunJobs` method
- [ ] 2.3.4 Implement `getJob` method
- [ ] 2.3.5 Implement `rerunWorkflow` method
- [ ] 2.3.6 Implement `rerunFailedJobs` method
- [ ] 2.3.7 Implement `cancelWorkflowRun` method

### 2.4 Error Handling
- [ ] 2.4.1 Create `api-client/error/GitHubApiError.scala` sealed trait
- [ ] 2.4.2 Implement error response parsing
- [ ] 2.4.3 Add retry logic with exponential backoff
- [ ] 2.4.4 Implement rate limit tracking
- [ ] 2.4.5 Add ETag caching support

### 2.5 Testing
- [ ] 2.5.1 Create mock HTTP responses for testing
- [ ] 2.5.2 Write unit tests for API client methods
- [ ] 2.5.3 Write integration tests with WireMock
- [ ] 2.5.4 Test error handling scenarios
- [ ] 2.5.5 Test rate limit handling

## Phase 3: CLI Interface (Week 3)

### 3.1 CLI Framework
- [ ] 3.1.1 Create `cli/Main.scala` entry point
- [ ] 3.1.2 Set up decline command structure
- [ ] 3.1.3 Create `cli/commands/Command.scala` base trait
- [ ] 3.1.4 Implement global options (--help, --version)
- [ ] 3.1.5 Add repository detection from git config

### 3.2 Subcommands
- [ ] 3.2.1 Implement `cli/commands/DashboardCommand.scala`
- [ ] 3.2.2 Implement `cli/commands/ListCommand.scala`
- [ ] 3.2.3 Implement `cli/commands/ShowCommand.scala`
- [ ] 3.2.4 Implement `cli/commands/RestartCommand.scala`
- [ ] 3.2.5 Implement `cli/commands/CancelCommand.scala`
- [ ] 3.2.6 Implement `cli/commands/WatchCommand.scala`

### 3.3 Output Formatting
- [ ] 3.3.1 Create `cli/output/Formatter.scala` trait
- [ ] 3.3.2 Implement `cli/output/TableFormatter.scala`
- [ ] 3.3.3 Implement `cli/output/JsonFormatter.scala`
- [ ] 3.3.4 Implement `cli/output/YamlFormatter.scala`
- [ ] 3.3.5 Add color support with fansi

### 3.4 Configuration
- [ ] 3.4.1 Create `cli/config/Config.scala` case class
- [ ] 3.4.2 Implement environment variable reading (GITHUB_TOKEN)
- [ ] 3.4.3 Add config file support (optional)
- [ ] 3.4.4 Implement repository auto-detection
- [ ] 3.4.5 Add configuration validation

## Phase 4: Terminal Dashboard (Week 4)

### 4.1 Terminal UI Framework
- [ ] 4.1.1 Evaluate terminal UI libraries (tui-scala, crossterm-scala, custom)
- [ ] 4.1.2 Set up chosen library or implement custom renderer
- [ ] 4.1.3 Create `terminal-ui/Terminal.scala` algebra
- [ ] 4.1.4 Implement terminal initialization and cleanup
- [ ] 4.1.5 Add terminal size detection

### 4.2 UI Components
- [ ] 4.2.1 Create `terminal-ui/components/Component.scala` trait
- [ ] 4.2.2 Implement `terminal-ui/components/HeaderComponent.scala`
- [ ] 4.2.3 Implement `terminal-ui/components/SummaryComponent.scala`
- [ ] 4.2.4 Implement `terminal-ui/components/WorkflowListComponent.scala`
- [ ] 4.2.5 Implement `terminal-ui/components/ActiveJobsComponent.scala`
- [ ] 4.2.6 Implement `terminal-ui/components/FooterComponent.scala`
- [ ] 4.2.7 Create `terminal-ui/components/Dashboard.scala` composite

### 4.3 Rendering
- [ ] 4.3.1 Create `terminal-ui/renderer/Renderer.scala`
- [ ] 4.3.2 Implement layout calculation
- [ ] 4.3.3 Add color and styling support
- [ ] 4.3.4 Implement Unicode/ASCII fallback
- [ ] 4.3.5 Add progress bar rendering

### 4.4 Input Handling
- [ ] 4.4.1 Create `terminal-ui/input/KeyEvent.scala` enum
- [ ] 4.4.2 Implement keyboard event reading
- [ ] 4.4.3 Create `terminal-ui/input/KeyHandler.scala`
- [ ] 4.4.4 Implement navigation (arrow keys, page up/down)
- [ ] 4.4.5 Implement action keys (r=refresh, q=quit, etc.)

### 4.5 State Management
- [ ] 4.5.1 Create `terminal-ui/state/DashboardState.scala`
- [ ] 4.5.2 Implement state updates with Ref
- [ ] 4.5.3 Add selection tracking
- [ ] 4.5.4 Implement filter state management
- [ ] 4.5.5 Add error state handling

## Phase 5: Workflow Management Service (Week 5)

### 5.1 Service Layer
- [ ] 5.1.1 Create `core/service/WorkflowService.scala` trait
- [ ] 5.1.2 Implement `core/service/WorkflowServiceImpl.scala`
- [ ] 5.1.3 Add caching with Ref and timestamps
- [ ] 5.1.4 Implement statistics calculations
- [ ] 5.1.5 Add watch mode with fs2 streams

### 5.2 Dashboard Service
- [ ] 5.2.1 Create `core/service/DashboardService.scala`
- [ ] 5.2.2 Implement auto-refresh logic
- [ ] 5.2.3 Add filter application
- [ ] 5.2.4 Implement detail view data fetching
- [ ] 5.2.5 Add error handling and recovery

### 5.3 Repository Service
- [ ] 5.3.1 Create `core/service/RepositoryService.scala`
- [ ] 5.3.2 Implement git config parsing
- [ ] 5.3.3 Add remote URL parsing
- [ ] 5.3.4 Implement repository validation
- [ ] 5.3.5 Add current branch detection

## Phase 6: Integration and Advanced Features (Week 6)

### 6.1 Dashboard Integration
- [ ] 6.1.1 Wire dashboard components with workflow service
- [ ] 6.1.2 Implement auto-refresh with fs2 streams
- [ ] 6.1.3 Add keyboard event handling
- [ ] 6.1.4 Implement detail view navigation
- [ ] 6.1.5 Add filter UI and logic

### 6.2 Watch Mode
- [ ] 6.2.1 Implement watch command logic
- [ ] 6.2.2 Add polling with configurable interval
- [ ] 6.2.3 Implement status change detection
- [ ] 6.2.4 Add completion notification
- [ ] 6.2.5 Implement timeout handling

### 6.3 Action Commands
- [ ] 6.3.1 Implement restart workflow logic
- [ ] 6.3.2 Add restart failed jobs logic
- [ ] 6.3.3 Implement cancel workflow logic
- [ ] 6.3.4 Add confirmation prompts for destructive actions
- [ ] 6.3.5 Implement action result feedback

### 6.4 Error Handling
- [ ] 6.4.1 Add error banner to dashboard
- [ ] 6.4.2 Implement error dismissal
- [ ] 6.4.3 Add rate limit warnings
- [ ] 6.4.4 Implement graceful degradation
- [ ] 6.4.5 Add error logging to file

### 6.5 Testing
- [ ] 6.5.1 Write unit tests for workflow service
- [ ] 6.5.2 Write unit tests for dashboard service
- [ ] 6.5.3 Write integration tests for CLI commands
- [ ] 6.5.4 Add property-based tests for state transitions
- [ ] 6.5.5 Test error scenarios

## Phase 7: Distribution and Documentation (Week 7)

### 7.1 Native Image Build
- [ ] 7.1.1 Add sbt-native-image plugin
- [ ] 7.1.2 Configure native-image settings
- [ ] 7.1.3 Add reflection configuration
- [ ] 7.1.4 Test native binary on macOS
- [ ] 7.1.5 Test native binary on Linux
- [ ] 7.1.6 Document native-image limitations

### 7.2 JAR Distribution
- [ ] 7.2.1 Configure sbt-assembly plugin
- [ ] 7.2.2 Create executable JAR
- [ ] 7.2.3 Add launcher script
- [ ] 7.2.4 Test JAR on different JVM versions
- [ ] 7.2.5 Document JAR usage

### 7.3 GitHub Actions CI
- [ ] 7.3.1 Create `.github/workflows/scala-github-actions-cli.yml`
- [ ] 7.3.2 Add compile and test jobs
- [ ] 7.3.3 Add native-image build job
- [ ] 7.3.4 Add artifact upload
- [ ] 7.3.5 Configure caching for dependencies

### 7.4 Documentation
- [ ] 7.4.1 Write comprehensive README.md
- [ ] 7.4.2 Add installation instructions
- [ ] 7.4.3 Document all CLI commands with examples
- [ ] 7.4.4 Add dashboard keyboard shortcuts reference
- [ ] 7.4.5 Create troubleshooting guide
- [ ] 7.4.6 Add architecture documentation
- [ ] 7.4.7 Document API client usage
- [ ] 7.4.8 Add contributing guidelines

### 7.5 Release Preparation
- [ ] 7.5.1 Create GitHub release workflow
- [ ] 7.5.2 Add version management
- [ ] 7.5.3 Create changelog
- [ ] 7.5.4 Prepare release notes
- [ ] 7.5.5 Tag v0.1.0 release

## Phase 8: Optional Enhancements (Future)

### 8.1 Advanced Features
- [ ] 8.1.1 Add workflow file syntax highlighting in detail view
- [ ] 8.1.2 Implement log streaming for running jobs
- [ ] 8.1.3 Add notification support (desktop notifications)
- [ ] 8.1.4 Implement multi-repository dashboard
- [ ] 8.1.5 Add workflow comparison view

### 8.2 Performance Optimizations
- [ ] 8.2.1 Implement incremental updates (only fetch changed runs)
- [ ] 8.2.2 Add persistent cache to disk
- [ ] 8.2.3 Optimize rendering for large lists
- [ ] 8.2.4 Add lazy loading for job details
- [ ] 8.2.5 Implement connection pooling optimizations

### 8.3 User Experience
- [ ] 8.3.1 Add customizable themes
- [ ] 8.3.2 Implement saved filters
- [ ] 8.3.3 Add workflow favorites
- [ ] 8.3.4 Implement search functionality
- [ ] 8.3.5 Add export to CSV/Excel

### 8.4 GitHub Enterprise Support
- [ ] 8.4.1 Add GitHub Enterprise Server URL configuration
- [ ] 8.4.2 Test with GitHub Enterprise API
- [ ] 8.4.3 Document Enterprise-specific setup
- [ ] 8.4.4 Add Enterprise authentication options

## Testing Strategy

### Unit Tests
- Domain models: Test case class construction and validation
- Service layer: Test business logic with mocked dependencies
- Formatters: Test output formatting for various inputs
- Components: Test rendering logic with fixed state

### Integration Tests
- API client: Test with WireMock or recorded responses
- CLI commands: Test end-to-end command execution
- Dashboard: Test component integration

### Property-Based Tests
- State transitions: Test all valid state changes
- Filter logic: Test filter combinations
- Pagination: Test edge cases

### Manual Testing
- Terminal compatibility: Test on different terminals
- Performance: Test with large workflow lists
- Error scenarios: Test network failures, rate limits
- User experience: Test keyboard navigation flow

## Success Metrics

- [ ] Dashboard refreshes in <1 second
- [ ] Native binary starts in <1 second
- [ ] All API operations complete successfully
- [ ] Tests achieve >80% code coverage
- [ ] Documentation is comprehensive and clear
- [ ] Zero critical bugs in initial release

