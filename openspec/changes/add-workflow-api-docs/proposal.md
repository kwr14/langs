# Add Workflow API Documentation

## Why
The Cassandra best-practice project includes an http4s REST API for workflow management (`ServerComponent.scala`), but lacks API documentation. Developers and API consumers need clear documentation of endpoints, request/response schemas, and usage examples to effectively integrate with the workflow engine.

## What Changes
- Add OpenAPI 3.1 specification for the workflow management REST API
- Document all three endpoints: POST /workflows, GET /workflows/{id}, GET /workflows/{id}/result
- Include request/response schemas for WorkflowDefinition, Workflow, and WorkflowResult
- Add example requests and responses
- Optionally integrate Swagger UI for interactive API exploration

## Impact
- **Affected specs**: `workflow-api` (new capability)
- **Affected code**: 
  - `scala/cassandra-best-practise/core/src/main/scala/uk/sky/kurate/ServerComponent.scala` (minimal changes)
  - New OpenAPI spec file: `scala/cassandra-best-practise/openapi.yaml`
  - Optional: Swagger UI integration in http4s routes
- **Breaking changes**: None - purely additive
- **Dependencies**: May add tapir or similar library for OpenAPI generation (optional)

## Success Criteria
- [ ] OpenAPI 3.1 spec file exists and validates
- [ ] All three endpoints are documented with schemas
- [ ] Example requests/responses are provided
- [ ] Documentation is accessible (file or Swagger UI)
- [ ] CI validates OpenAPI spec on changes

