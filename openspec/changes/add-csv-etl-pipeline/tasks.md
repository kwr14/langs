# Implementation Tasks: Add CSV ETL Pipeline

**Change ID**: `add-csv-etl-pipeline`

## Phase 1: Setup & Dependencies

- [ ] Add scala-csv dependency to build.sbt
- [ ] Create `scala/automata/etl` package structure
- [ ] Create sample input CSV file with test data

## Phase 2: Data Models

- [ ] Create `Person` case class (firstName, lastName, age)
- [ ] Create `TransformedPerson` case class (fullName, age)
- [ ] Add JSON codecs for models (using jsoniter-scala)

## Phase 3: Extract Component

- [ ] Create `CSVExtractor` trait
- [ ] Implement `CSVExtractorImpl` using scala-csv
- [ ] Write unit tests for CSVExtractor
- [ ] Test with sample input.csv

## Phase 4: Transform Component

- [ ] Create `PersonTransformer` trait
- [ ] Implement transformation logic (firstName + lastName → fullName)
- [ ] Write unit tests for PersonTransformer
- [ ] Test edge cases (empty names, special characters)

## Phase 5: Load Components

- [ ] Create `CSVLoader` trait
- [ ] Implement `CSVLoaderImpl` to write output CSV
- [ ] Write unit tests for CSVLoader
- [ ] Create `SQLiteLoader` trait
- [ ] Implement `SQLiteLoaderImpl` with table creation and insert
- [ ] Write unit tests for SQLiteLoader

## Phase 6: ETL Workflow

- [ ] Create `ETLWorkflow` orchestrator
- [ ] Implement end-to-end pipeline using cats-effect IO
- [ ] Add error handling and logging
- [ ] Write integration test for full workflow

## Phase 7: Sample Data & Scripts

- [ ] Create `input.csv` with 10+ sample records
- [ ] Create `run-etl.sh` script to execute workflow
- [ ] Update README with ETL example
- [ ] Add documentation for running the ETL

## Phase 8: Testing & Validation

- [ ] Run all unit tests
- [ ] Run integration test
- [ ] Verify output.csv is created correctly
- [ ] Verify SQLite table is populated
- [ ] Manual smoke test with sample data

## Completion Checklist

- [ ] All code written and tested
- [ ] All tests passing
- [ ] Documentation updated
- [ ] Sample data created
- [ ] Run script working
- [ ] Code reviewed
- [ ] Ready for merge

