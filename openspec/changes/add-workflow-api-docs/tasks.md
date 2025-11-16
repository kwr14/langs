# Implementation Tasks

## 1. Research & Design
- [x] 1.1 Review existing API endpoints in ServerComponent.scala
- [x] 1.2 Document current request/response data models
- [x] 1.3 Decide on approach: manual OpenAPI spec vs. code-first with tapir
- [x] 1.4 Choose OpenAPI version (3.0 vs 3.1)

## 2. Create OpenAPI Specification
- [x] 2.1 Create `scala/cassandra-best-practise/openapi.yaml`
- [x] 2.2 Define API metadata (title, version, description, servers)
- [x] 2.3 Document POST /workflows endpoint
  - [x] 2.3.1 Request body schema (WorkflowDefinition)
  - [x] 2.3.2 Query parameters (variables)
  - [x] 2.3.3 Response schema (Workflow)
  - [x] 2.3.4 Example request/response
- [x] 2.4 Document GET /workflows/{id} endpoint
  - [x] 2.4.1 Path parameter (id: UUID)
  - [x] 2.4.2 Success response (Workflow)
  - [x] 2.4.3 404 response (not found)
  - [x] 2.4.4 Example request/response
- [x] 2.5 Document GET /workflows/{id}/result endpoint
  - [x] 2.5.1 Path parameter (id: UUID)
  - [x] 2.5.2 Success response (WorkflowResult)
  - [x] 2.5.3 404 response (not found)
  - [x] 2.5.4 Example request/response

## 3. Define Schemas
- [x] 3.1 WorkflowDefinition schema
- [x] 3.2 Workflow schema
- [x] 3.3 WorkflowResult schema
- [x] 3.4 Task schema
- [x] 3.5 TaskDefinition schema
- [x] 3.6 TaskResult schema
- [x] 3.7 Status enum (Pending, Running, Completed, Failed)
- [x] 3.8 Error response schema

## 4. Validation & Testing
- [x] 4.1 Install OpenAPI validator (e.g., swagger-cli or openapi-generator)
- [x] 4.2 Validate OpenAPI spec locally
- [ ] 4.3 Test example requests against actual API
- [ ] 4.4 Verify schemas match actual data structures

## 5. Integration (Optional)
- [ ] 5.1 Add Swagger UI dependency to build.sbt (if desired)
- [ ] 5.2 Create Swagger UI route in ServerComponent
- [ ] 5.3 Serve OpenAPI spec at /api-docs endpoint
- [ ] 5.4 Test Swagger UI in browser

## 6. CI/CD
- [ ] 6.1 Add OpenAPI validation to CI workflow
- [ ] 6.2 Create GitHub workflow to validate openapi.yaml
- [ ] 6.3 Ensure validation runs on PR changes

## 7. Documentation
- [x] 7.1 Update scala/cassandra-best-practise/README.md
- [x] 7.2 Add API documentation section
- [x] 7.3 Include link to OpenAPI spec
- [x] 7.4 Add usage examples

