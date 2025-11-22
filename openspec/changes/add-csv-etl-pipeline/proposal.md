# Change Proposal: Add CSV ETL Pipeline

**Change ID**: `add-csv-etl-pipeline`  
**Status**: Draft  
**Created**: 2025-11-22  
**Author**: AI Assistant

## Summary

Add a simple ETL (Extract, Transform, Load) pipeline to the automata project that:
1. Reads a CSV file with `first_name`, `last_name`, `age` fields
2. Transforms it to a new CSV with `full_name`, `age` fields
3. Loads the transformed data into a SQLite database table

## Motivation

Demonstrate a practical ETL use case using the automata workflow engine with:
- File-based data extraction
- Simple transformation logic (combining first/last names)
- Database persistence (SQLite)
- End-to-end workflow orchestration

## Scope

### In Scope
- CSV reader for extracting source data
- Transformation logic to combine first_name + last_name → full_name
- CSV writer for transformed output
- SQLite table creation and data loading
- Sample input CSV with test data
- Workflow definition for the ETL pipeline
- Unit tests for each component

### Out of Scope
- Error handling for malformed CSV files (future enhancement)
- Data validation rules (future enhancement)
- Incremental/delta loading (future enhancement)
- Multiple file formats (JSON, Parquet, etc.)
- Distributed processing

## Design Overview

### Components

1. **CSVExtractor**: Reads CSV file and returns list of Person records
2. **PersonTransformer**: Transforms Person → TransformedPerson (combines names)
3. **CSVLoader**: Writes TransformedPerson records to output CSV
4. **SQLiteLoader**: Creates table and inserts records into SQLite
5. **ETLWorkflow**: Orchestrates the entire pipeline

### Data Models

```scala
case class Person(firstName: String, lastName: String, age: Int)
case class TransformedPerson(fullName: String, age: Int)
```

### Workflow Steps

1. **Extract**: Read `input.csv` → List[Person]
2. **Transform**: List[Person] → List[TransformedPerson]
3. **Load CSV**: List[TransformedPerson] → `output.csv`
4. **Load DB**: List[TransformedPerson] → SQLite table `people`

## Implementation Plan

See `tasks.md` for detailed implementation checklist.

## Testing Strategy

- Unit tests for each component (extractor, transformer, loaders)
- Integration test for full ETL workflow
- Sample data with edge cases (empty names, various ages)

## Success Criteria

- [ ] Can read CSV with first_name, last_name, age
- [ ] Can transform to full_name, age format
- [ ] Can write transformed CSV output
- [ ] Can create SQLite table and insert records
- [ ] All tests passing
- [ ] Sample workflow runs successfully

## Dependencies

- Existing: cats-effect, fs2, sqlite-jdbc (already in build.sbt)
- New: scala-csv library for CSV parsing

## Risks & Mitigations

**Risk**: CSV parsing edge cases (quotes, commas in fields)  
**Mitigation**: Use established scala-csv library with proper escaping

**Risk**: SQLite file locking in concurrent scenarios  
**Mitigation**: Single-threaded execution for this simple example

## Alternatives Considered

1. **Use existing workflow engine**: Decided to create standalone ETL components that can be integrated later
2. **Use Spark/Flink**: Too heavy for this simple example
3. **Manual CSV parsing**: Reinventing the wheel, use library instead

## Open Questions

- Should we support configurable input/output paths? (Answer: Yes, via parameters)
- Should we support schema validation? (Answer: Not in v1, future enhancement)

