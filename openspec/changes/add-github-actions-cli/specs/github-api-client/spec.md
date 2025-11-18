# GitHub API Client Specification

## ADDED Requirements

### Requirement: OpenAPI-Based Client Generation

The system SHALL generate a type-safe GitHub API client from GitHub's official OpenAPI specification.

#### Scenario: Client generation from OpenAPI spec
- **WHEN** the build process runs
- **THEN** the system generates Scala 3 client code from the GitHub OpenAPI specification

#### Scenario: Type-safe API methods
- **WHEN** developer calls an API method
- **THEN** the compiler enforces correct parameter types and return types

#### Scenario: Automatic JSON codec derivation
- **WHEN** API responses are received
- **THEN** the system automatically decodes JSON to Scala case classes

### Requirement: Workflow Runs API Integration

The system SHALL integrate with GitHub's workflow runs API endpoints.

#### Scenario: List workflow runs
- **WHEN** client calls listWorkflowRuns with owner, repo, and optional filters
- **THEN** the system returns a list of workflow runs matching the criteria

#### Scenario: Get workflow run details
- **WHEN** client calls getWorkflowRun with owner, repo, and run ID
- **THEN** the system returns detailed information for the specified run

#### Scenario: List workflow run jobs
- **WHEN** client calls listWorkflowRunJobs with owner, repo, and run ID
- **THEN** the system returns all jobs for the specified workflow run

#### Scenario: Get job details
- **WHEN** client calls getJob with owner, repo, and job ID
- **THEN** the system returns detailed information including steps for the job

### Requirement: Workflow Run Actions

The system SHALL support actions on workflow runs via GitHub API.

#### Scenario: Rerun workflow
- **WHEN** client calls rerunWorkflow with owner, repo, and run ID
- **THEN** the system triggers a rerun of the entire workflow

#### Scenario: Rerun failed jobs
- **WHEN** client calls rerunFailedJobs with owner, repo, and run ID
- **THEN** the system triggers a rerun of only the failed jobs

#### Scenario: Cancel workflow run
- **WHEN** client calls cancelWorkflowRun with owner, repo, and run ID
- **THEN** the system cancels the in-progress workflow run

### Requirement: Authentication and Authorization

The system SHALL authenticate all API requests using GitHub personal access tokens.

#### Scenario: Token-based authentication
- **WHEN** client makes an API request
- **THEN** the system includes the token in the Authorization header

#### Scenario: Unauthorized request
- **WHEN** API returns 401 Unauthorized
- **THEN** the system raises an AuthenticationError with clear message

#### Scenario: Forbidden request
- **WHEN** API returns 403 Forbidden
- **THEN** the system raises an AuthorizationError indicating insufficient permissions

### Requirement: Rate Limit Handling

The system SHALL track and respect GitHub API rate limits.

#### Scenario: Rate limit headers parsing
- **WHEN** API response includes rate limit headers
- **THEN** the system extracts and stores remaining requests and reset time

#### Scenario: Rate limit exceeded
- **WHEN** API returns 429 Too Many Requests
- **THEN** the system raises a RateLimitError with reset time information

#### Scenario: Rate limit status query
- **WHEN** application queries rate limit status
- **THEN** the system returns current limit, remaining, and reset time

### Requirement: Error Handling and Retry Logic

The system SHALL handle API errors gracefully with appropriate retry logic.

#### Scenario: Transient network error
- **WHEN** API request fails with network timeout
- **THEN** the system retries up to 3 times with exponential backoff

#### Scenario: Server error (5xx)
- **WHEN** API returns 500-level error
- **THEN** the system retries up to 3 times with exponential backoff

#### Scenario: Client error (4xx)
- **WHEN** API returns 400-level error (except 429)
- **THEN** the system does not retry and raises appropriate error

#### Scenario: Resource not found
- **WHEN** API returns 404 Not Found
- **THEN** the system raises a NotFoundError with resource details

### Requirement: Response Caching and Conditional Requests

The system SHALL use ETags for efficient API usage and caching.

#### Scenario: ETag caching
- **WHEN** API response includes ETag header
- **THEN** the system stores the ETag for subsequent requests

#### Scenario: Conditional request with ETag
- **WHEN** making a request for previously fetched resource
- **THEN** the system includes If-None-Match header with stored ETag

#### Scenario: Not modified response
- **WHEN** API returns 304 Not Modified
- **THEN** the system uses cached response without counting against rate limit

### Requirement: Pagination Support

The system SHALL handle paginated API responses automatically.

#### Scenario: Paginated workflow runs
- **WHEN** workflow runs exceed single page limit
- **THEN** the system follows pagination links to fetch all pages

#### Scenario: Page size configuration
- **WHEN** client specifies page size parameter
- **THEN** the system requests the specified number of items per page

#### Scenario: Lazy pagination
- **WHEN** client requests paginated results
- **THEN** the system returns a stream that fetches pages on demand

### Requirement: HTTP Client Configuration

The system SHALL configure the HTTP client for optimal performance and reliability.

#### Scenario: Connection pooling
- **WHEN** multiple API requests are made
- **THEN** the system reuses HTTP connections from a connection pool

#### Scenario: Request timeout
- **WHEN** API request exceeds 30 seconds
- **THEN** the system cancels the request and raises a TimeoutError

#### Scenario: User-Agent header
- **WHEN** making API requests
- **THEN** the system includes a User-Agent header identifying the application

### Requirement: Type-Safe Domain Models

The system SHALL define type-safe domain models for all GitHub API entities.

#### Scenario: Workflow run model
- **WHEN** API returns workflow run data
- **THEN** the system decodes it to a WorkflowRun case class with typed fields

#### Scenario: Enum-based status fields
- **WHEN** API returns status or conclusion fields
- **THEN** the system uses Scala 3 enums for type-safe status representation

#### Scenario: Optional field handling
- **WHEN** API returns nullable fields
- **THEN** the system represents them as Option types in Scala

