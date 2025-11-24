# Proposal: Refactor CSV Loader/Extractor to be Generic

**Change ID:** `refactor-csv-generic`  
**Status:** Draft  
**Created:** 2025-11-24  
**Author:** AI Assistant

## Why

The current `CSVLoader` and `CSVExtractor` implementations are tightly coupled to specific case classes (`Person` and `TransformedPerson`). This creates several problems:

1. **Limited Reusability**: Cannot use the CSV loader/extractor for other data types without duplicating code
2. **Tight Coupling**: Hard-coded field names and types make the code inflexible
3. **Scalability Issues**: Adding new CSV-based ETL pipelines requires creating new loader/extractor classes
4. **Violation of DRY**: CSV reading/writing logic would be duplicated across multiple implementations
5. **Testing Complexity**: Each concrete implementation needs separate tests

**Business Impact:**
- Slows down development of new ETL pipelines
- Increases maintenance burden
- Makes the codebase harder to understand and extend

## What Changes

Transform the CSV loader and extractor into generic, type-parameterized components that work with any case class through a typeclass-based approach.

### High-Level Changes

1. **Create `CSVCodec[A]` typeclass** - Defines how to convert between case class `A` and CSV rows
2. **Refactor `CSVLoader[F[_], A]`** - Make it generic over data type `A`
3. **Refactor `CSVExtractor[F[_], A]`** - Make it generic over data type `A`
4. **Provide codec instances** - For `Person` and `TransformedPerson`
5. **Update existing code** - Migrate ETL pipeline to use generic versions
6. **Add comprehensive tests** - Test with multiple data types

### Files to Modify

- `scala/automata/core/src/main/scala/uk/sky/etl/load/CSVLoader.scala`
- `scala/automata/core/src/main/scala/uk/sky/etl/extract/CSVExtractor.scala`
- `scala/automata/core/src/main/scala/uk/sky/etl/models/Models.scala`
- `scala/automata/core/src/main/scala/uk/sky/etl/ETLWorkflow.scala`
- Test files for loader and extractor

### Files to Create

- `scala/automata/core/src/main/scala/uk/sky/etl/csv/CSVCodec.scala` - Typeclass definition
- `scala/automata/core/src/test/scala/uk/sky/etl/csv/CSVCodecSpec.scala` - Codec tests

## Problem Statement

**Current State:**

```scala
trait CSVLoader[F[_]] {
  def load(data: List[TransformedPerson], outputPath: String): F[Unit]
}

trait CSVExtractor[F[_]] {
  def extract(filePath: String): F[List[Person]]
}
```

**Problems:**
- Hard-coded to specific types (`Person`, `TransformedPerson`)
- Cannot reuse for other data types
- Field mapping is hard-coded in implementation

## Proposed Solution

**New Design:**

```scala
// Typeclass for CSV encoding/decoding
trait CSVCodec[A] {
  def headers: List[String]
  def encode(value: A): List[String]
  def decode(row: Map[String, String]): Either[String, A]
}

// Generic loader
trait CSVLoader[F[_], A] {
  def load(data: List[A], outputPath: String)(implicit codec: CSVCodec[A]): F[Unit]
}

// Generic extractor
trait CSVExtractor[F[_], A] {
  def extract(filePath: String)(implicit codec: CSVCodec[A]): F[List[A]]
}
```

**Benefits:**
- ✅ Works with any case class
- ✅ Type-safe encoding/decoding
- ✅ Reusable across all ETL pipelines
- ✅ Easy to add new data types (just provide a codec instance)
- ✅ Testable with multiple types

## Goals

### Primary Goals
1. Make CSV loader and extractor generic over data type
2. Maintain backward compatibility with existing ETL pipeline
3. Provide type-safe CSV encoding/decoding
4. Enable easy extension for new data types

### Secondary Goals
1. Improve error handling (use `Either` for decoding failures)
2. Add validation support in codecs
3. Provide helper macros for automatic codec derivation (future enhancement)

### Non-Goals
1. Support for complex nested structures (keep it simple for flat CSV)
2. Custom delimiters or CSV formats (use scala-csv defaults)
3. Streaming support (keep current batch processing)

## Architecture

### Component Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    CSVCodec[A]                          │
│  - headers: List[String]                                │
│  - encode(A): List[String]                              │
│  - decode(Map[String,String]): Either[String, A]        │
└─────────────────────────────────────────────────────────┘
                          ▲
                          │ implicit
                          │
         ┌────────────────┴────────────────┐
         │                                 │
┌────────┴──────────┐           ┌─────────┴─────────┐
│ CSVExtractor[F,A] │           │  CSVLoader[F,A]   │
│                   │           │                   │
│ extract(path)     │           │  load(data, path) │
│  → F[List[A]]     │           │   → F[Unit]       │
└───────────────────┘           └───────────────────┘
```

### Codec Instances

```scala
object CSVCodec {
  // Person codec
  implicit val personCodec: CSVCodec[Person] = new CSVCodec[Person] {
    def headers = List("first_name", "last_name", "age")
    def encode(p: Person) = List(p.firstName, p.lastName, p.age.toString)
    def decode(row: Map[String, String]) = 
      for {
        firstName <- row.get("first_name").toRight("Missing first_name")
        lastName <- row.get("last_name").toRight("Missing last_name")
        ageStr <- row.get("age").toRight("Missing age")
        age <- ageStr.toIntOption.toRight(s"Invalid age: $ageStr")
      } yield Person(firstName, lastName, age)
  }
  
  // TransformedPerson codec
  implicit val transformedPersonCodec: CSVCodec[TransformedPerson] = ...
}
```

## Dependencies

**Existing Dependencies (no changes):**
- scala-csv 1.3.10
- cats-effect 3.5.4

**No new dependencies required.**

## Migration Path

1. **Phase 1: Add Generic Components** (backward compatible)
   - Create `CSVCodec` typeclass
   - Create generic `CSVLoader` and `CSVExtractor`
   - Keep old implementations temporarily

2. **Phase 2: Migrate Existing Code**
   - Update `ETLWorkflow` to use generic versions
   - Update tests
   - Verify all tests pass

3. **Phase 3: Cleanup**
   - Remove old hard-coded implementations
   - Update documentation

## Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Breaking changes to existing code | High | Keep old implementations during migration |
| Performance regression | Medium | Benchmark before/after |
| Complex codec implementation | Low | Provide clear examples and documentation |
| Type inference issues | Medium | Use explicit type parameters where needed |

## Success Criteria

1. ✅ All existing tests pass with generic implementation
2. ✅ Can create CSV loader/extractor for new types with < 10 lines of code
3. ✅ No performance regression (< 5% slower)
4. ✅ Zero breaking changes to public API during migration
5. ✅ Documentation includes examples for 3+ different data types

## Timeline

- **Spec Creation**: 1 hour
- **Implementation**: 3-4 hours
- **Testing**: 2 hours
- **Documentation**: 1 hour
- **Total**: ~1 day

## Open Questions

1. Should we support optional fields in CSV (e.g., `Option[String]`)?
2. Should we add automatic codec derivation using Shapeless or Magnolia?
3. Should we support custom field name mapping (e.g., `firstName` → `first_name`)?

## References

- Current implementation: `scala/automata/core/src/main/scala/uk/sky/etl/`
- Similar pattern: Circe's `Encoder`/`Decoder` typeclasses
- CSV library: https://github.com/tototoshi/scala-csv

