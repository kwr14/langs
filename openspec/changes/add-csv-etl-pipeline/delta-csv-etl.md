# Spec Delta: CSV ETL Pipeline

**Capability**: `csv-etl-pipeline`  
**Change**: `add-csv-etl-pipeline`

## ADDED Requirements

### REQ-ETL-001: CSV Extraction
**Priority**: Must Have  
**Category**: Data Extraction

The system SHALL provide a CSV extractor that reads CSV files and parses them into structured data.

#### Scenario: Extract person data from CSV
**Given** a CSV file with headers `first_name,last_name,age`  
**And** the file contains valid person records  
**When** the extractor reads the file  
**Then** it SHALL return a list of Person objects  
**And** each Person SHALL have firstName, lastName, and age fields populated

#### Scenario: Handle empty CSV file
**Given** an empty CSV file (only headers)  
**When** the extractor reads the file  
**Then** it SHALL return an empty list  
**And** it SHALL NOT throw an exception

### REQ-ETL-002: Person Transformation
**Priority**: Must Have  
**Category**: Data Transformation

The system SHALL provide a transformer that combines first and last names into a full name.

#### Scenario: Transform person to full name format
**Given** a Person with firstName="John" and lastName="Doe" and age=30  
**When** the transformer processes the person  
**Then** it SHALL return a TransformedPerson with fullName="John Doe" and age=30

#### Scenario: Handle single-word names
**Given** a Person with firstName="Madonna" and lastName="" and age=65  
**When** the transformer processes the person  
**Then** it SHALL return a TransformedPerson with fullName="Madonna" and age=65

### REQ-ETL-003: CSV Loading
**Priority**: Must Have  
**Category**: Data Loading

The system SHALL provide a CSV loader that writes transformed data to a CSV file.

#### Scenario: Write transformed data to CSV
**Given** a list of TransformedPerson objects  
**When** the loader writes to output.csv  
**Then** the file SHALL contain headers `full_name,age`  
**And** each row SHALL contain the fullName and age values  
**And** the file SHALL be valid CSV format

### REQ-ETL-004: SQLite Database Loading
**Priority**: Must Have  
**Category**: Data Persistence

The system SHALL provide a SQLite loader that creates a table and inserts transformed data.

#### Scenario: Create table and insert records
**Given** a list of TransformedPerson objects  
**And** a SQLite database file path  
**When** the loader executes  
**Then** it SHALL create a table named `people` if it doesn't exist  
**And** the table SHALL have columns `full_name TEXT` and `age INTEGER`  
**And** it SHALL insert all records into the table  
**And** the inserted data SHALL be queryable

#### Scenario: Handle existing table
**Given** a SQLite database with an existing `people` table  
**When** the loader executes  
**Then** it SHALL append new records to the existing table  
**And** it SHALL NOT drop or truncate the table

### REQ-ETL-005: End-to-End Workflow
**Priority**: Must Have  
**Category**: Workflow Orchestration

The system SHALL provide an ETL workflow that orchestrates extraction, transformation, and loading.

#### Scenario: Execute complete ETL pipeline
**Given** an input CSV file with person data  
**And** output paths for CSV and SQLite database  
**When** the ETL workflow executes  
**Then** it SHALL extract data from input CSV  
**And** it SHALL transform all records  
**And** it SHALL write transformed data to output CSV  
**And** it SHALL load transformed data to SQLite database  
**And** it SHALL complete without errors

#### Scenario: Handle errors gracefully
**Given** an invalid input file path  
**When** the ETL workflow executes  
**Then** it SHALL return a failure result  
**And** it SHALL include an error message describing the issue  
**And** it SHALL NOT create partial output files

## Data Models

### Person
```scala
case class Person(
  firstName: String,
  lastName: String,
  age: Int
)
```

### TransformedPerson
```scala
case class TransformedPerson(
  fullName: String,
  age: Int
)
```

## API Contracts

### CSVExtractor
```scala
trait CSVExtractor[F[_]] {
  def extract(filePath: String): F[List[Person]]
}
```

### PersonTransformer
```scala
trait PersonTransformer[F[_]] {
  def transform(persons: List[Person]): F[List[TransformedPerson]]
}
```

### CSVLoader
```scala
trait CSVLoader[F[_]] {
  def load(data: List[TransformedPerson], outputPath: String): F[Unit]
}
```

### SQLiteLoader
```scala
trait SQLiteLoader[F[_]] {
  def load(data: List[TransformedPerson], dbPath: String): F[Unit]
}
```

### ETLWorkflow
```scala
trait ETLWorkflow[F[_]] {
  def run(inputPath: String, outputCsvPath: String, dbPath: String): F[Unit]
}
```

