# Implementation Tasks

## 1. Research & Design
- [ ] 1.1 Review existing API endpoints in ServerComponent.scala
- [ ] 1.2 Document current request/response data models
- [ ] 1.3 Decide on approach: manual OpenAPI spec vs. code-first with tapir
- [ ] 1.4 Choose OpenAPI version (3.0 vs 3.1)

## 2. Create OpenAPI Specification
- [ ] 2.1 Create `scala/cassandra-best-practise/openapi.yaml`
- [ ] 2.2 Define API metadata (title, version, description, servers)
- [ ] 2.3 Document POST /workflows endpoint
  - [ ] 2.3.1 Request body schema (WorkflowDefinition)
  - [ ] 2.3.2 Query parameters (variables)
  - [ ] 2.3.3 Response schema (Workflow)
  - [ ] 2.3.4 Example request/response
- [ ] 2.4 Document GET /workflows/{id} endpoint
  - [ ] 2.4.1 Path parameter (id: UUID)
  - [ ] 2.4.2 Success response (Workflow)
  - [ ] 2.4.3 404 response (not found)
  - [ ] 2.4.4 Example request/response
- [ ] 2.5 Document GET /workflows/{id}/result endpoint
  - [ ] 2.5.1 Path parameter (id: UUID)
  - [ ] 2.5.2 Success response (WorkflowResult)
  - [ ] 2.5.3 404 response (not found)
  - [ ] 2.5.4 Example request/response

## 3. Define Schemas
- [ ] 3.1 WorkflowDefinition schema
- [ ] 3.2 Workflow schema
- [ ] 3.3 WorkflowResult schema
- [ ] 3.4 Task schema
- [ ] 3.5 TaskDefinition schema
- [ ] 3.6 TaskResult schema
- [ ] 3.7 Status enum (Pending, Running, Completed, Failed)
- [ ] 3.8 Error response schema

## 4. Validation & Testing
- [ ] 4.1 Install OpenAPI validator (e.g., swagger-cli or openapi-generator)
- [ ] 4.2 Validate OpenAPI spec locally
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
- [ ] 7.1 Update scala/cassandra-best-practise/README.md
- [ ] 7.2 Add API documentation section
- [ ] 7.3 Include link to OpenAPI spec
- [ ] 7.4 Add usage examples

