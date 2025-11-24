# Capability: CSV Processing (Generic)

**Capability ID:** `csv-processing`  
**Version:** 2.0  
**Status:** Draft

## Overview

Generic CSV processing capability that supports reading from and writing to CSV files for any case class type through a typeclass-based approach.

## MODIFIED Requirements

### REQ-CSV-001: Generic CSV Codec Typeclass

**Description:** Define a typeclass `CSVCodec[A]` that enables encoding and decoding of any type `A` to/from CSV format.

**Rationale:** Enables reusable CSV processing for any data type without code duplication.

**Priority:** High

#### Scenario: Define CSVCodec typeclass

**Given** a case class type `A`  
**When** a `CSVCodec[A]` instance is defined  
**Then** it must provide:
- `headers: List[String]` - column names for CSV header
- `encode(value: A): List[String]` - convert instance to CSV row
- `decode(row: Map[String, String]): Either[String, A]` - parse CSV row to instance

#### Scenario: Encode case class to CSV row

**Given** a `Person("John", "Doe", 30)` instance  
**And** an implicit `CSVCodec[Person]` in scope  
**When** `encode` is called  
**Then** it returns `List("John", "Doe", "30")`

#### Scenario: Decode CSV row to case class

**Given** a CSV row `Map("first_name" -> "John", "last_name" -> "Doe", "age" -> "30")`  
**And** an implicit `CSVCodec[Person]` in scope  
**When** `decode` is called  
**Then** it returns `Right(Person("John", "Doe", 30))`

#### Scenario: Handle decode errors for missing fields

**Given** a CSV row `Map("first_name" -> "John", "age" -> "30")` (missing last_name)  
**And** an implicit `CSVCodec[Person]` in scope  
**When** `decode` is called  
**Then** it returns `Left("Missing last_name")`

#### Scenario: Handle decode errors for invalid types

**Given** a CSV row `Map("first_name" -> "John", "last_name" -> "Doe", "age" -> "invalid")`  
**And** an implicit `CSVCodec[Person]` in scope  
**When** `decode` is called  
**Then** it returns `Left("Invalid age: invalid")`

### REQ-CSV-002: Generic CSV Loader

**Description:** CSV loader must be generic over data type `A` and use `CSVCodec[A]` for encoding.

**Rationale:** Enables writing any case class to CSV without creating type-specific loaders.

**Priority:** High

#### Scenario: Load generic data to CSV

**Given** a list of values of type `A`  
**And** an implicit `CSVCodec[A]` in scope  
**And** an output file path  
**When** `CSVLoader[F, A].load(data, path)` is called  
**Then** the CSV file is created with correct headers  
**And** each value is encoded using the codec  
**And** the file is properly closed

#### Scenario: Load TransformedPerson to CSV

**Given** a list `List(TransformedPerson("John Doe", 30), TransformedPerson("Jane Smith", 25))`  
**And** an implicit `CSVCodec[TransformedPerson]` in scope  
**When** `CSVLoader[IO, TransformedPerson].load(data, "output.csv")` is called  
**Then** the file contains:
```
full_name,age
John Doe,30
Jane Smith,25
```

#### Scenario: Handle write errors

**Given** a list of values  
**And** an invalid output path (e.g., read-only directory)  
**When** `load` is called  
**Then** it returns a failed effect `F[Unit]` with appropriate error

### REQ-CSV-003: Generic CSV Extractor

**Description:** CSV extractor must be generic over data type `A` and use `CSVCodec[A]` for decoding.

**Rationale:** Enables reading any case class from CSV without creating type-specific extractors.

**Priority:** High

#### Scenario: Extract generic data from CSV

**Given** a CSV file with headers matching codec  
**And** an implicit `CSVCodec[A]` in scope  
**When** `CSVExtractor[F, A].extract(path)` is called  
**Then** each row is decoded using the codec  
**And** successful decodes are collected  
**And** the file is properly closed  
**And** returns `F[List[A]]`

#### Scenario: Extract Person from CSV

**Given** a CSV file:
```
first_name,last_name,age
John,Doe,30
Jane,Smith,25
```
**And** an implicit `CSVCodec[Person]` in scope  
**When** `CSVExtractor[IO, Person].extract("input.csv")` is called  
**Then** it returns `IO(List(Person("John", "Doe", 30), Person("Jane", "Smith", 25)))`

#### Scenario: Handle decode errors gracefully

**Given** a CSV file with some invalid rows  
**When** `extract` is called  
**Then** it should either:
- Skip invalid rows and log warnings, OR
- Fail fast with first error, OR
- Collect all errors and return them

**Note:** Implementation should choose one strategy and document it.

#### Scenario: Handle missing file

**Given** a non-existent file path  
**When** `extract` is called  
**Then** it returns a failed effect with file not found error

### REQ-CSV-004: Codec Instances for Existing Models

**Description:** Provide `CSVCodec` instances for `Person` and `TransformedPerson`.

**Rationale:** Maintain backward compatibility with existing ETL pipeline.

**Priority:** High

#### Scenario: Person codec instance

**Given** the `Person` case class  
**When** `CSVCodec.personCodec` is imported  
**Then** it provides encoding/decoding for Person  
**And** headers are `List("first_name", "last_name", "age")`

#### Scenario: TransformedPerson codec instance

**Given** the `TransformedPerson` case class  
**When** `CSVCodec.transformedPersonCodec` is imported  
**Then** it provides encoding/decoding for TransformedPerson  
**And** headers are `List("full_name", "age")`

### REQ-CSV-005: Type Safety

**Description:** The generic implementation must maintain compile-time type safety.

**Rationale:** Prevent runtime errors by catching type mismatches at compile time.

**Priority:** High

#### Scenario: Type mismatch caught at compile time

**Given** a `CSVLoader[IO, Person]`  
**When** attempting to load `List[TransformedPerson]`  
**Then** compilation fails with type error

#### Scenario: Codec instance required at compile time

**Given** a call to `CSVLoader[IO, MyType].load(data, path)`  
**When** no implicit `CSVCodec[MyType]` is in scope  
**Then** compilation fails with "implicit not found" error

### REQ-CSV-006: Resource Safety

**Description:** All file operations must properly close resources even on errors.

**Rationale:** Prevent resource leaks and file handle exhaustion.

**Priority:** High

#### Scenario: File closed on successful load

**Given** a successful `load` operation  
**When** the operation completes  
**Then** the file handle is closed

#### Scenario: File closed on failed load

**Given** a `load` operation that throws an exception  
**When** the exception occurs  
**Then** the file handle is still closed

#### Scenario: File closed on successful extract

**Given** a successful `extract` operation  
**When** the operation completes  
**Then** the file handle is closed

#### Scenario: File closed on failed extract

**Given** an `extract` operation that throws an exception  
**When** the exception occurs  
**Then** the file handle is still closed

## REMOVED Requirements

None. This is a refactoring that maintains all existing functionality.

## ADDED Requirements

### REQ-CSV-007: Easy Codec Creation

**Description:** Creating a new codec instance should require minimal boilerplate.

**Rationale:** Encourage adoption and reduce friction for new data types.

**Priority:** Medium

#### Scenario: Create codec for simple case class

**Given** a case class `Employee(name: String, salary: Int)`  
**When** creating a `CSVCodec[Employee]`  
**Then** it requires < 10 lines of code

#### Scenario: Codec with validation

**Given** a case class with validation rules (e.g., age > 0)  
**When** creating a codec  
**Then** validation can be added in the `decode` method  
**And** returns `Left(errorMessage)` for invalid data

### REQ-CSV-008: Backward Compatibility

**Description:** Existing ETL pipeline must work without changes during migration.

**Rationale:** Enable gradual migration without breaking existing functionality.

**Priority:** High

#### Scenario: Existing ETL workflow continues to work

**Given** the current `ETLWorkflow` implementation  
**When** generic components are introduced  
**Then** the workflow compiles and runs without changes  
**And** produces identical output

#### Scenario: Existing tests pass

**Given** all existing test suites  
**When** generic components are introduced  
**Then** all tests pass without modification

## Dependencies

- scala-csv 1.3.10 (existing)
- cats-effect 3.5.4 (existing)

## Migration Notes

1. Introduce generic components alongside existing implementations
2. Update ETL workflow to use generic versions
3. Verify all tests pass
4. Remove old implementations
5. Update documentation

## Performance Considerations

- Codec encoding/decoding should add < 5% overhead vs hard-coded implementation
- File I/O remains the bottleneck (codec overhead negligible)
- No additional allocations beyond necessary List/Map creation

