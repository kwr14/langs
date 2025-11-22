# CSV ETL Pipeline

A simple, spec-driven ETL (Extract, Transform, Load) pipeline that processes CSV data and loads it into both CSV and SQLite database formats.

## Overview

This ETL pipeline demonstrates:
- **Extract**: Reading CSV files with person data (first_name, last_name, age)
- **Transform**: Combining first and last names into a full_name field
- **Load**: Writing transformed data to both CSV and SQLite database

## Architecture

The implementation follows a **tagless final** pattern with functional effects using Cats Effect:

```
CSVExtractor[F]      → Extract data from CSV
PersonTransformer[F] → Transform Person to TransformedPerson
CSVLoader[F]         → Load data to CSV file
SQLiteLoader[F]      → Load data to SQLite database
ETLWorkflow[F]       → Orchestrate the complete pipeline
```

## Data Models

### Input Model
```scala
case class Person(
  firstName: String,
  lastName: String,
  age: Int
)
```

### Output Model
```scala
case class TransformedPerson(
  fullName: String,
  age: Int
)
```

## Usage

### Running the ETL Pipeline

```bash
# Run with default paths
./run-etl.sh

# Run with custom paths
./run-etl.sh input.csv output.csv database.db
```

### Default Paths
- **Input CSV**: `data/input.csv`
- **Output CSV**: `data/output.csv`
- **Database**: `data/people.db`

### Running from sbt

```bash
sbt "project core" "runMain uk.sky.etl.ETLApp data/input.csv data/output.csv data/people.db"
```

## Input CSV Format

The input CSV must have the following columns:
```csv
first_name,last_name,age
John,Doe,30
Jane,Smith,25
```

## Output

### CSV Output
```csv
full_name,age
John Doe,30
Jane Smith,25
```

### SQLite Database
Table: `people`
- `id` (INTEGER PRIMARY KEY AUTOINCREMENT)
- `full_name` (TEXT NOT NULL)
- `age` (INTEGER NOT NULL)

## Testing

Run all tests:
```bash
sbt "project core" test
```

### Test Coverage

- **CSVExtractorSpec**: Tests CSV extraction with various scenarios
- **PersonTransformerSpec**: Tests name transformation logic
- **CSVLoaderSpec**: Tests CSV output generation
- **SQLiteLoaderSpec**: Tests database operations
- **ETLWorkflowSpec**: Integration tests for the complete pipeline

All 15 tests validate:
- ✅ Normal data processing
- ✅ Empty files
- ✅ Missing fields
- ✅ Special characters
- ✅ Error handling
- ✅ End-to-end workflow

## Spec-Driven Development

This feature was developed following the OpenSpec methodology:

### Specification Documents
- **Proposal**: `openspec/changes/add-csv-etl-pipeline/proposal.md`
- **Tasks**: `openspec/changes/add-csv-etl-pipeline/tasks.md`
- **Delta Spec**: `openspec/changes/add-csv-etl-pipeline/delta-csv-etl.md`

### Requirements
- REQ-ETL-001: CSV Extraction
- REQ-ETL-002: Person Transformation
- REQ-ETL-003: CSV Loading
- REQ-ETL-004: SQLite Database Loading
- REQ-ETL-005: End-to-End Workflow

## Dependencies

- **scala-csv**: CSV parsing and writing
- **sqlite-jdbc**: SQLite database connectivity
- **cats-effect**: Functional effects
- **scalatest**: Testing framework

## Example

```scala
import cats.effect.IO
import uk.sky.etl.ETLWorkflow

val workflow = ETLWorkflow[IO]

workflow.run(
  inputPath = "data/input.csv",
  outputCsvPath = "data/output.csv",
  dbPath = "data/people.db"
).unsafeRunSync()
```

## Querying the Database

```bash
# Count records
sqlite3 data/people.db "SELECT COUNT(*) FROM people;"

# View all records
sqlite3 data/people.db "SELECT * FROM people;"

# Query by age
sqlite3 data/people.db "SELECT * FROM people WHERE age > 30;"
```

## Features

- ✅ Functional programming with Cats Effect
- ✅ Tagless final pattern for effect abstraction
- ✅ Resource-safe file and database operations
- ✅ Comprehensive test coverage (15 tests)
- ✅ Error handling and validation
- ✅ Spec-driven development with OpenSpec
- ✅ Sample data included
- ✅ Shell script for easy execution

