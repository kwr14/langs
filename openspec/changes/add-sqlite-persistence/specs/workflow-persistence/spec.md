# Spec Delta: SQLite-Based Persistence for Automata

**Capability**: `workflow-persistence`
**Change**: `add-sqlite-persistence`

## ADDED Requirements

### REQ-PERSIST-001: Database Schema

**Description**: The system SHALL provide a SQLite database schema to persist workflow execution state.

**Rationale**: Durable storage is required to survive process restarts and enable recovery.

**Priority**: High

#### Scenario: Create database tables on initialization

**Given** the SQLite persistence layer is initialized  
**When** the schema manager creates tables  
**Then** the following tables SHALL exist:
- `workflows` table with columns: id, name, status, created_at, updated_at, tasks_json, variables_json
- `tasks` table with columns: id, workflow_id, name, status, created_at, updated_at, task_type, parameters_json, retries, max_retries, dependencies_json
- `workflow_results` table with columns: workflow_id, task_results_json, error, created_at
- `task_results` table with columns: task_id, output, error, created_at
- `transitions` table with columns: id, entity_id, from_status, to_status, timestamp

**And** appropriate indexes SHALL be created for performance

---

### REQ-PERSIST-002: Workflow Persistence

**Description**: The system SHALL persist workflow state to SQLite database.

**Rationale**: Workflows must be recoverable after process restart.

**Priority**: High

#### Scenario: Save new workflow

**Given** a new workflow is created  
**When** the workflow is saved to the database  
**Then** the workflow SHALL be inserted into the `workflows` table  
**And** all associated tasks SHALL be inserted into the `tasks` table  
**And** the workflow SHALL be retrievable by ID

#### Scenario: Update existing workflow

**Given** a workflow exists in the database  
**When** the workflow status is updated  
**Then** the workflow record SHALL be updated in the database  
**And** the `updated_at` timestamp SHALL be refreshed  
**And** the updated workflow SHALL be retrievable with new status

#### Scenario: Retrieve workflow with tasks

**Given** a workflow with multiple tasks exists in the database  
**When** the workflow is retrieved by ID  
**Then** the workflow SHALL include all associated tasks  
**And** task dependencies SHALL be correctly deserialized  
**And** task status SHALL match the persisted state

---

### REQ-PERSIST-003: Task Persistence

**Description**: The system SHALL persist individual task state to SQLite database.

**Rationale**: Tasks must be independently queryable and updatable.

**Priority**: High

#### Scenario: Update task status

**Given** a task exists in the database with status "Pending"  
**When** the task status is updated to "Running"  
**Then** the task record SHALL be updated in the database  
**And** the task SHALL be retrievable with status "Running"  
**And** the parent workflow SHALL reflect the updated task

#### Scenario: Save task result

**Given** a task has completed execution  
**When** the task result is saved  
**Then** the result SHALL be inserted into the `task_results` table  
**And** the result SHALL be retrievable by task ID  
**And** the result SHALL include output or error information

---

### REQ-PERSIST-004: State Transition Audit Trail

**Description**: The system SHALL record all state transitions in an audit trail.

**Rationale**: Complete history of state changes is required for debugging and compliance.

**Priority**: Medium

#### Scenario: Record workflow state transition

**Given** a workflow transitions from "Pending" to "Running"  
**When** the transition is saved  
**Then** a transition record SHALL be inserted into the `transitions` table  
**And** the transition SHALL include entity_id, from_status, to_status, and timestamp  
**And** the transition SHALL be retrievable by entity_id

#### Scenario: Query transition history

**Given** a workflow has undergone multiple state transitions  
**When** transitions are queried for the workflow ID  
**Then** all transitions SHALL be returned in chronological order  
**And** each transition SHALL show the complete state change

---

### REQ-PERSIST-005: Workflow Recovery

**Description**: The system SHALL support resuming workflows from persisted state after restart.

**Rationale**: Critical for fault tolerance and long-running workflows.

**Priority**: High

#### Scenario: Resume workflow after process restart

**Given** a workflow is running with some tasks completed  
**And** the process is terminated  
**When** the process restarts and the workflow is loaded  
**Then** the workflow SHALL be in the same state as before termination  
**And** completed tasks SHALL remain completed  
**And** pending tasks SHALL be available for execution  
**And** the workflow SHALL continue from where it left off

#### Scenario: Recover failed workflow

**Given** a workflow failed during execution  
**When** the workflow is retrieved from the database  
**Then** the workflow status SHALL be "Failed"  
**And** the error information SHALL be available  
**And** the workflow MAY be retried or marked as resolved

---

### REQ-PERSIST-006: Connection Management

**Description**: The system SHALL manage database connections safely using resource management.

**Rationale**: Prevent connection leaks and ensure proper cleanup.

**Priority**: High

#### Scenario: Acquire and release connection

**Given** a persistence operation is requested  
**When** the operation executes  
**Then** a database connection SHALL be acquired from the pool  
**And** the connection SHALL be released after the operation completes  
**And** the connection SHALL be released even if the operation fails

---

### REQ-PERSIST-007: JSON Serialization

**Description**: The system SHALL serialize complex types to JSON for database storage.

**Rationale**: SQLite does not natively support complex Scala types.

**Priority**: High

#### Scenario: Serialize workflow with tasks

**Given** a workflow contains a list of tasks  
**When** the workflow is saved to the database  
**Then** the tasks list SHALL be serialized to JSON  
**And** the JSON SHALL be stored in the `tasks_json` column  
**And** the workflow SHALL be correctly deserialized when retrieved

#### Scenario: Serialize task dependencies

**Given** a task has dependencies on other tasks (Set[UUID])  
**When** the task is saved to the database  
**Then** the dependencies SHALL be serialized to JSON  
**And** the dependencies SHALL be correctly deserialized as Set[UUID]

---

### REQ-PERSIST-008: Performance

**Description**: The system SHALL provide acceptable performance for typical operations.

**Rationale**: Persistence should not significantly impact workflow execution speed.

**Priority**: Medium

#### Scenario: Fast workflow retrieval

**Given** a workflow exists in the database  
**When** the workflow is retrieved by ID  
**Then** the operation SHALL complete in less than 100ms  
**And** the workflow SHALL include all associated data

#### Scenario: Efficient bulk operations

**Given** multiple tasks need to be saved  
**When** tasks are saved in a batch  
**Then** the operation SHALL use a single transaction  
**And** the operation SHALL complete efficiently

---

## Implementation Notes

- Use `cats.effect.Resource` for connection management
- Use `jsoniter-scala` for JSON serialization (already available)
- Use prepared statements to prevent SQL injection
- Use WAL mode for better concurrent access
- Create indexes on frequently queried columns (id, workflow_id, status)
- Use UPSERT semantics where appropriate (INSERT OR REPLACE)

