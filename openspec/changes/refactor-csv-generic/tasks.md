# Tasks: Refactor CSV Loader/Extractor to be Generic

**Change ID:** `refactor-csv-generic`

## Phase 1: Core Typeclass Implementation

### 1.1 Create CSVCodec Typeclass
- [ ] Create `scala/automata/core/src/main/scala/uk/sky/etl/csv/CSVCodec.scala`
- [ ] Define `CSVCodec[A]` trait with `headers`, `encode`, and `decode` methods
- [ ] Add companion object with helper methods
- [ ] Add documentation with examples

### 1.2 Implement Codec Instances
- [ ] Create `personCodec` instance for `Person` case class
- [ ] Create `transformedPersonCodec` instance for `TransformedPerson` case class
- [ ] Add error handling for missing/invalid fields
- [ ] Add validation logic (e.g., age > 0)

### 1.3 Create Generic CSVLoader
- [ ] Refactor `CSVLoader[F[_]]` to `CSVLoader[F[_], A]`
- [ ] Add implicit `CSVCodec[A]` parameter
- [ ] Update implementation to use codec for encoding
- [ ] Maintain resource safety (proper file closing)

### 1.4 Create Generic CSVExtractor
- [ ] Refactor `CSVExtractor[F[_]]` to `CSVExtractor[F[_], A]`
- [ ] Add implicit `CSVCodec[A]` parameter
- [ ] Update implementation to use codec for decoding
- [ ] Add error handling for decode failures
- [ ] Maintain resource safety (proper file closing)

## Phase 2: Testing

### 2.1 Test CSVCodec
- [ ] Create `scala/automata/core/src/test/scala/uk/sky/etl/csv/CSVCodecSpec.scala`
- [ ] Test `Person` codec encoding
- [ ] Test `Person` codec decoding (success cases)
- [ ] Test `Person` codec decoding (error cases: missing fields, invalid types)
- [ ] Test `TransformedPerson` codec encoding
- [ ] Test `TransformedPerson` codec decoding

### 2.2 Test Generic CSVLoader
- [ ] Update `CSVLoaderSpec` to use generic version
- [ ] Test loading `TransformedPerson` data
- [ ] Test loading with different data type (e.g., create test case class)
- [ ] Test error handling (write failures)
- [ ] Verify file output format matches expected CSV

### 2.3 Test Generic CSVExtractor
- [ ] Update `CSVExtractorSpec` to use generic version
- [ ] Test extracting `Person` data
- [ ] Test extracting with different data type
- [ ] Test error handling (missing fields, invalid data)
- [ ] Test empty file handling

### 2.4 Integration Tests
- [ ] Update `ETLWorkflowSpec` to use generic components
- [ ] Verify end-to-end pipeline still works
- [ ] Test with multiple data types in same workflow
- [ ] Verify all existing tests pass

## Phase 3: Migration and Cleanup

### 3.1 Update ETL Workflow
- [ ] Update `ETLWorkflow.scala` to use generic `CSVLoader[F, TransformedPerson]`
- [ ] Update `ETLWorkflow.scala` to use generic `CSVExtractor[F, Person]`
- [ ] Update type signatures
- [ ] Verify compilation

### 3.2 Update ETL App
- [ ] Update `ETLApp.scala` to use generic components
- [ ] Add explicit type parameters where needed
- [ ] Test running the app end-to-end

### 3.3 Update Models
- [ ] Move codec instances to `Models.scala` companion object
- [ ] Ensure codecs are in implicit scope
- [ ] Add documentation for codec usage

### 3.4 Remove Old Code
- [ ] Remove old non-generic `CSVLoaderImpl` (if separate)
- [ ] Remove old non-generic `CSVExtractorImpl` (if separate)
- [ ] Clean up unused imports

## Phase 4: Documentation and Examples

### 4.1 Update Documentation
- [ ] Update `ETL_README.md` with generic usage examples
- [ ] Add section on creating custom codecs
- [ ] Add examples for 3 different data types
- [ ] Document error handling patterns

### 4.2 Create Example
- [ ] Create example showing custom data type with codec
- [ ] Create example showing codec composition
- [ ] Create example showing error handling

### 4.3 Add Code Comments
- [ ] Add scaladoc to `CSVCodec` trait
- [ ] Add scaladoc to codec instances
- [ ] Add usage examples in comments

## Phase 5: Validation and Performance

### 5.1 Run All Tests
- [ ] Run full test suite: `sbt "project core" test`
- [ ] Verify all tests pass
- [ ] Fix any failing tests

### 5.2 Performance Testing
- [ ] Benchmark CSV loading (before/after)
- [ ] Benchmark CSV extraction (before/after)
- [ ] Verify < 5% performance regression
- [ ] Document performance characteristics

### 5.3 Code Review
- [ ] Review for type safety
- [ ] Review for error handling completeness
- [ ] Review for resource safety
- [ ] Review for code clarity

### 5.4 Final Validation
- [ ] Run `openspec validate refactor-csv-generic --strict`
- [ ] Fix any validation issues
- [ ] Verify all tasks completed

## Phase 6: Commit and Documentation

### 6.1 Commit Changes
- [ ] Stage all changes
- [ ] Write comprehensive commit message
- [ ] Include before/after examples
- [ ] Reference OpenSpec proposal

### 6.2 Update OpenSpec
- [ ] Mark all tasks as complete
- [ ] Update proposal status to "Implemented"
- [ ] Add implementation notes

## Success Criteria Checklist

- [ ] All existing tests pass
- [ ] Can create CSV loader/extractor for new types with < 10 lines of code
- [ ] No performance regression (< 5% slower)
- [ ] Zero breaking changes to public API
- [ ] Documentation includes examples for 3+ different data types
- [ ] Error handling is comprehensive and type-safe
- [ ] Code is well-documented with scaladoc

## Notes

- Keep backward compatibility during migration
- Use explicit type parameters if type inference fails
- Consider adding codec derivation in future (Shapeless/Magnolia)
- Document common patterns for codec implementation

