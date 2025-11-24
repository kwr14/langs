# Implementation Tasks: Add SQLite-Based Persistence

**Change ID**: `add-sqlite-persistence`

## Phase 1: Database Schema and Core Implementation

- [ ] **Task 1.1**: Design database schema
  - [ ] Create `workflows` table schema
  - [ ] Create `tasks` table schema
  - [ ] Create `workflow_results` table schema
  - [ ] Create `task_results` table schema
  - [ ] Create `transitions` table schema
  - [ ] Define indexes for performance

- [ ] **Task 1.2**: Implement SchemaManager
  - [ ] Create `SchemaManager` class
  - [ ] Implement `createTables()` method
  - [ ] Implement `dropTables()` method (for testing)
  - [ ] Add schema version tracking
  - [ ] Add idempotent table creation (IF NOT EXISTS)

- [ ] **Task 1.3**: Implement SQLitePersistence class
  - [ ] Create `SQLitePersistence` class extending `PersistenceLayer`
  - [ ] Implement connection management with Resource
  - [ ] Implement `saveWorkflow()` method
  - [ ] Implement `getWorkflow()` method
  - [ ] Implement `listWorkflows()` method
  - [ ] Implement `updateWorkflow()` method
  - [ ] Implement `deleteWorkflow()` method

- [ ] **Task 1.4**: Implement task persistence methods
  - [ ] Implement `saveTask()` method
  - [ ] Implement `getTask()` method
  - [ ] Implement `updateTask()` method
  - [ ] Implement `deleteTask()` method

- [ ] **Task 1.5**: Implement result persistence methods
  - [ ] Implement `saveWorkflowResult()` method
  - [ ] Implement `getWorkflowResult()` method
  - [ ] Implement `saveTaskResult()` method
  - [ ] Implement `getTaskResult()` method

- [ ] **Task 1.6**: Implement transition persistence methods
  - [ ] Implement `saveTransition()` method
  - [ ] Implement `getTransitions()` method

- [ ] **Task 1.7**: Implement JSON serialization helpers
  - [ ] Create serialization utilities for Workflow
  - [ ] Create serialization utilities for Task
  - [ ] Create serialization utilities for WorkflowResult
  - [ ] Create serialization utilities for TaskResult
  - [ ] Create serialization utilities for Status enum
  - [ ] Create serialization utilities for UUID

## Phase 2: Testing

- [ ] **Task 2.1**: Create unit tests for SchemaManager
  - [ ] Test table creation
  - [ ] Test idempotent creation
  - [ ] Test schema version tracking

- [ ] **Task 2.2**: Create unit tests for SQLitePersistence
  - [ ] Test workflow CRUD operations
  - [ ] Test task CRUD operations
  - [ ] Test workflow result operations
  - [ ] Test task result operations
  - [ ] Test transition operations
  - [ ] Test concurrent access scenarios

- [ ] **Task 2.3**: Create integration tests
  - [ ] Test complete workflow lifecycle with persistence
  - [ ] Test workflow recovery after simulated crash
  - [ ] Test state transition audit trail
  - [ ] Test multiple workflows concurrently

- [ ] **Task 2.4**: Create performance tests
  - [ ] Benchmark workflow save/load operations
  - [ ] Benchmark task save/load operations
  - [ ] Benchmark query performance with large datasets

## Phase 3: Integration and Documentation

- [ ] **Task 3.1**: Update WorkflowEngine to use SQLitePersistence
  - [ ] Add configuration for database path
  - [ ] Update factory methods to create SQLitePersistence
  - [ ] Add migration path from InMemoryPersistence

- [ ] **Task 3.2**: Create examples
  - [ ] Create example showing basic workflow with persistence
  - [ ] Create example showing workflow recovery
  - [ ] Create example showing audit trail queries

- [ ] **Task 3.3**: Create documentation
  - [ ] Document database schema
  - [ ] Document configuration options
  - [ ] Document recovery procedures
  - [ ] Update README with persistence information

- [ ] **Task 3.4**: Add utility scripts
  - [ ] Create script to inspect database
  - [ ] Create script to backup database
  - [ ] Create script to restore database

## Phase 4: Validation

- [ ] **Task 4.1**: Run all existing tests with SQLite backend
- [ ] **Task 4.2**: Validate performance benchmarks
- [ ] **Task 4.3**: Validate recovery scenarios
- [ ] **Task 4.4**: Code review and cleanup

## Total Tasks: 60+

**Estimated Effort**: 4-6 hours

