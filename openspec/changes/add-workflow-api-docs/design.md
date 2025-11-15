# Design: Workflow API Documentation

## Context
The cassandra-best-practise project includes a workflow management REST API built with http4s and Cats Effect. The API has three endpoints for creating and querying workflows, but lacks formal documentation. API consumers need clear documentation to understand request/response formats and integrate with the service.

**Current State:**
- http4s REST API in `ServerComponent.scala`
- Three endpoints: POST /workflows, GET /workflows/{id}, GET /workflows/{id}/result
- JSON serialization with jsoniter-scala
- No API documentation or schema definitions

**Stakeholders:**
- Developers integrating with the workflow API
- Future maintainers of the codebase
- API consumers (internal or external)

## Goals / Non-Goals

### Goals
- Provide comprehensive API documentation in OpenAPI 3.1 format
- Document all endpoints with request/response schemas
- Include realistic examples for all operations
- Validate OpenAPI spec in CI to prevent drift
- Make documentation easily accessible

### Non-Goals
- Changing the existing API implementation
- Adding authentication/authorization (out of scope)
- Generating client SDKs (can be done later)
- Migrating to a different API framework
- Adding new API endpoints

## Decisions

### Decision 1: Manual OpenAPI Spec vs. Code-First
**Choice:** Manual OpenAPI specification (openapi.yaml)

**Rationale:**
- Existing codebase uses http4s with jsoniter-scala (no built-in OpenAPI support)
- Adding tapir would require significant refactoring of existing routes
- Manual spec is simpler for a small API (3 endpoints)
- Can be created without modifying working code
- Easier to review and maintain for this use case

**Alternatives Considered:**
- **tapir**: Code-first approach with type-safe endpoints
  - Pros: Generates spec from code, type-safe, prevents drift
  - Cons: Requires rewriting existing routes, adds dependency, learning curve
- **rho**: DSL for http4s with OpenAPI generation
  - Pros: Integrates with http4s
  - Cons: Less maintained, still requires route refactoring
- **guardrail**: Spec-first with code generation
  - Pros: Generates server/client code
  - Cons: Inverts current workflow, requires build changes

### Decision 2: OpenAPI Version
**Choice:** OpenAPI 3.1

**Rationale:**
- Latest stable version with JSON Schema compatibility
- Better support for modern tooling
- Aligns with industry standards
- No reason to use older 3.0 version

### Decision 3: Swagger UI Integration
**Choice:** Optional - document but don't implement initially

**Rationale:**
- Swagger UI is useful but not essential for initial documentation
- Can be added later if needed
- Keeps initial change focused and small
- OpenAPI spec file is sufficient for most use cases
- Can use external tools (Swagger Editor, Postman) to view spec

**Implementation if needed later:**
- Add webjars dependency for Swagger UI
- Serve static Swagger UI at /docs endpoint
- Serve openapi.yaml at /api-docs endpoint

### Decision 4: Schema Mapping Strategy
**Choice:** Map Scala case classes to OpenAPI schemas manually

**Mapping rules:**
- `UUID` → `string` with `format: uuid`
- `Instant` → `string` with `format: date-time`
- `Map[String, Any]` → `object` with `additionalProperties: true`
- Case classes → `object` with properties
- Sealed traits/enums → `enum` with allowed values
- `Option[T]` → schema with `nullable: true` or not in `required` list

### Decision 5: CI Validation
**Choice:** Add GitHub Actions workflow to validate OpenAPI spec

**Tools:**
- Use `swagger-cli validate` or `openapi-generator validate`
- Run on every push that modifies openapi.yaml
- Run on PRs to catch issues early

**Workflow:**
```yaml
- name: Validate OpenAPI Spec
  run: |
    npm install -g @apidevtools/swagger-cli
    swagger-cli validate scala/cassandra-best-practise/openapi.yaml
```

## Risks / Trade-offs

### Risk: Spec Drift
**Description:** Manual OpenAPI spec may drift from actual implementation over time.

**Mitigation:**
- Add CI validation to ensure spec is valid
- Document update process in README
- Consider integration tests that validate actual responses against spec
- Future: Could migrate to tapir for automatic generation

### Risk: Incomplete Schema Definitions
**Description:** Complex types (Map[String, Any]) may be hard to represent accurately.

**Mitigation:**
- Use `additionalProperties: true` for flexible maps
- Document constraints in description fields
- Provide comprehensive examples
- Consider adding JSON Schema validation in tests

### Trade-off: Manual Maintenance
**Description:** Manual spec requires discipline to keep updated.

**Benefit:** No code changes, simpler implementation, faster delivery.

**Acceptance:** For a small API (3 endpoints), manual maintenance is acceptable. Can revisit if API grows significantly.

## Migration Plan

### Phase 1: Create Specification
1. Create `openapi.yaml` in scala/cassandra-best-practise/
2. Document all three endpoints
3. Define all schemas
4. Add examples
5. Validate locally

### Phase 2: CI Integration
1. Create GitHub Actions workflow
2. Add OpenAPI validation step
3. Test on sample PR

### Phase 3: Documentation
1. Update README with API documentation section
2. Link to OpenAPI spec
3. Add usage examples

### Rollback
- If issues arise, simply remove the openapi.yaml file
- No code changes means no risk to existing functionality

## Open Questions

1. **Should we version the API?** 
   - Current: No versioning in routes
   - Recommendation: Add version to OpenAPI spec (v1), but don't change routes yet

2. **Should we add request validation?**
   - Current: No explicit validation beyond JSON parsing
   - Recommendation: Document in spec, implement validation in future change

3. **Should we document error responses in detail?**
   - Current: Simple string error messages
   - Recommendation: Document current behavior, improve in future change

4. **Should we add rate limiting documentation?**
   - Current: No rate limiting
   - Recommendation: Not applicable, skip for now

