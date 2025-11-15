# Workflow API Specification

## ADDED Requirements

### Requirement: OpenAPI Specification
The workflow management REST API SHALL provide an OpenAPI 3.1 specification that documents all endpoints, request/response schemas, and usage examples.

#### Scenario: OpenAPI spec file exists
- **GIVEN** the cassandra-best-practise project
- **WHEN** a developer looks for API documentation
- **THEN** an `openapi.yaml` file SHALL exist in the project root
- **AND** the file SHALL be valid OpenAPI 3.1 format

#### Scenario: OpenAPI spec validates
- **GIVEN** the `openapi.yaml` file
- **WHEN** validated with an OpenAPI validator
- **THEN** the validation SHALL pass with no errors
- **AND** all schemas SHALL be properly defined

### Requirement: Workflow Creation Endpoint Documentation
The OpenAPI spec SHALL document the POST /workflows endpoint for creating new workflows.

#### Scenario: POST /workflows endpoint documented
- **GIVEN** the OpenAPI specification
- **WHEN** a developer reviews the POST /workflows endpoint
- **THEN** the endpoint SHALL include:
  - Request body schema (WorkflowDefinition)
  - Query parameter schema (variables: Map[String, Any])
  - Success response schema (Workflow with 200 status)
  - Example request with sample WorkflowDefinition
  - Example response with sample Workflow

#### Scenario: WorkflowDefinition schema defined
- **GIVEN** the OpenAPI specification
- **WHEN** a developer reviews the WorkflowDefinition schema
- **THEN** the schema SHALL include all required fields:
  - id (UUID)
  - name (string)
  - tasks (array of TaskDefinition)
- **AND** each field SHALL have type, description, and format information

### Requirement: Workflow Retrieval Endpoint Documentation
The OpenAPI spec SHALL document the GET /workflows/{id} endpoint for retrieving workflow status.

#### Scenario: GET /workflows/{id} endpoint documented
- **GIVEN** the OpenAPI specification
- **WHEN** a developer reviews the GET /workflows/{id} endpoint
- **THEN** the endpoint SHALL include:
  - Path parameter (id: UUID)
  - Success response schema (Workflow with 200 status)
  - Not found response (404 status with error message)
  - Example request with sample UUID
  - Example success and error responses

#### Scenario: Workflow schema defined
- **GIVEN** the OpenAPI specification
- **WHEN** a developer reviews the Workflow schema
- **THEN** the schema SHALL include all fields:
  - id (UUID)
  - definition (WorkflowDefinition)
  - status (Status enum)
  - tasks (array of Task)
  - createdAt (timestamp)
  - updatedAt (timestamp)

### Requirement: Workflow Result Endpoint Documentation
The OpenAPI spec SHALL document the GET /workflows/{id}/result endpoint for retrieving workflow results.

#### Scenario: GET /workflows/{id}/result endpoint documented
- **GIVEN** the OpenAPI specification
- **WHEN** a developer reviews the GET /workflows/{id}/result endpoint
- **THEN** the endpoint SHALL include:
  - Path parameter (id: UUID)
  - Success response schema (WorkflowResult with 200 status)
  - Not found response (404 status with error message)
  - Example request with sample UUID
  - Example success and error responses

#### Scenario: WorkflowResult schema defined
- **GIVEN** the OpenAPI specification
- **WHEN** a developer reviews the WorkflowResult schema
- **THEN** the schema SHALL include all fields:
  - workflowId (UUID)
  - status (Status enum)
  - results (array of TaskResult)
  - completedAt (timestamp, optional)
  - error (string, optional)

### Requirement: Schema Definitions
The OpenAPI spec SHALL define all data model schemas used by the API.

#### Scenario: All schemas are defined
- **GIVEN** the OpenAPI specification
- **WHEN** a developer reviews the components/schemas section
- **THEN** the following schemas SHALL be defined:
  - WorkflowDefinition
  - Workflow
  - WorkflowResult
  - TaskDefinition
  - Task
  - TaskResult
  - Status (enum: Pending, Running, Completed, Failed)
  - ErrorResponse

#### Scenario: Schemas match implementation
- **GIVEN** the OpenAPI schemas
- **WHEN** compared to the actual Scala case classes
- **THEN** all fields SHALL match the implementation
- **AND** all types SHALL be correctly mapped (UUID → string with format, etc.)

### Requirement: API Examples
The OpenAPI spec SHALL provide realistic examples for all endpoints.

#### Scenario: Request examples provided
- **GIVEN** the OpenAPI specification
- **WHEN** a developer reviews any endpoint
- **THEN** at least one example request SHALL be provided
- **AND** the example SHALL use realistic data values

#### Scenario: Response examples provided
- **GIVEN** the OpenAPI specification
- **WHEN** a developer reviews any endpoint
- **THEN** example responses SHALL be provided for:
  - Success cases (200 status)
  - Error cases (404, 400, 500 as applicable)

### Requirement: CI Validation
The OpenAPI specification SHALL be validated in CI to ensure it remains valid and up-to-date.

#### Scenario: CI validates OpenAPI spec
- **GIVEN** a GitHub Actions workflow
- **WHEN** changes are pushed to the repository
- **THEN** the OpenAPI spec SHALL be validated
- **AND** the build SHALL fail if validation errors exist

#### Scenario: CI runs on spec changes
- **GIVEN** the CI workflow configuration
- **WHEN** the openapi.yaml file is modified
- **THEN** the validation workflow SHALL trigger
- **AND** validation results SHALL be reported in the PR

