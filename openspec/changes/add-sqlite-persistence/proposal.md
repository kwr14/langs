# Change Proposal: Add SQLite-Based Persistence for Automata Workflow Engine

**Change ID**: `add-sqlite-persistence`  
**Status**: Draft  
**Created**: 2025-11-24  
**Author**: AI Assistant  

## Overview

Replace the in-memory persistence layer in the Automata workflow engine with a SQLite-based implementation to achieve true durability. This enables workflows and tasks to survive process restarts, crashes, and enables recovery from the last persisted state.

## Why

The current in-memory persistence implementation loses all workflow state on process restart, making it unsuitable for production use. Workflows cannot be recovered after crashes, and there is no persistent audit trail for debugging or compliance. This change adds SQLite-based persistence to provide true durability, enabling the workflow engine to survive failures and resume execution from the last persisted state.

## What Changes

- Add `SQLitePersistence` class implementing `PersistenceLayer` trait
- Add `SchemaManager` for database schema creation and migration
- Create SQLite database schema with 5 tables: workflows, tasks, workflow_results, task_results, transitions
- Add JSON serialization/deserialization for complex types (workflows, tasks, dependencies)
- Add resource-safe connection management using cats-effect Resource
- Add comprehensive test suite for persistence operations
- Add examples demonstrating workflow recovery and audit trail queries
- Update documentation with persistence configuration and usage

## Problem Statement

The current `InMemoryPersistence` implementation stores all workflow and task state in mutable maps:

```scala
class InMemoryPersistence extends PersistenceLayer {
  private val workflows = mutable.Map[ID, Workflow]()
  private val tasks = mutable.Map[ID, Task]()
  // ...
}
```

**Limitations:**
- ❌ All state is lost on process restart
- ❌ No crash recovery capability
- ❌ Cannot resume workflows after failures
- ❌ No persistent audit trail
- ❌ Single-node only (cannot distribute)

## Proposed Solution

Implement `SQLitePersistence` class that:
1. Persists workflows, tasks, results, and transitions to SQLite database
2. Provides schema migration and table creation
3. Maintains backward compatibility with `PersistenceLayer` trait
4. Enables workflow recovery and resumption
5. Provides persistent audit trail via transitions table

## Goals

- ✅ **Durability**: Workflows survive process restarts
- ✅ **Recoverability**: Resume workflows from last persisted state
- ✅ **Audit Trail**: Complete history of state transitions
- ✅ **Backward Compatibility**: Drop-in replacement for `InMemoryPersistence`
- ✅ **Testability**: Comprehensive test coverage
- ✅ **Performance**: Efficient queries with proper indexing

## Non-Goals

- ❌ Distributed persistence (multi-node coordination)
- ❌ Migration from other databases
- ❌ Real-time replication
- ❌ Advanced query capabilities beyond basic CRUD

## Architecture

### Database Schema

**Tables:**
1. `workflows` - Workflow metadata and state
2. `tasks` - Task metadata and state
3. `workflow_results` - Final workflow execution results
4. `task_results` - Individual task execution results
5. `transitions` - State transition audit trail

### Implementation Components

1. **SQLitePersistence** - Main persistence implementation
2. **SchemaManager** - Database schema creation and migration
3. **ConnectionPool** - Resource-safe connection management
4. **Serialization** - JSON serialization for complex types

## Dependencies

- `org.xerial:sqlite-jdbc:3.46.0.0` (already available)
- `cats-effect` for resource management
- `jsoniter-scala` for JSON serialization (already available)

## Success Criteria

1. All existing tests pass with SQLite backend
2. Workflows can be resumed after process restart
3. State transitions are fully auditable
4. Performance is acceptable (< 100ms for typical operations)
5. Comprehensive test coverage (> 80%)

## Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| SQLite file corruption | High | Regular backups, WAL mode |
| Concurrent access issues | Medium | Use connection pooling, proper locking |
| Schema evolution | Medium | Version-based migrations |
| Performance degradation | Low | Proper indexing, connection pooling |

## Timeline

- **Phase 1**: Schema design and SQLitePersistence implementation (2-3 hours)
- **Phase 2**: Testing and validation (1-2 hours)
- **Phase 3**: Documentation and examples (1 hour)

**Total Estimated Effort**: 4-6 hours

## Open Questions

1. Should we support custom database paths?
2. Should we implement connection pooling or use single connection?
3. Should we support database migrations for schema evolution?
4. Should we add database backup/restore utilities?

## References

- Existing implementation: `scala/automata/core/src/main/scala/uk/sky/kurate/PersistenceLayer.scala`
- Similar pattern: `scala/durabletask/src/main/scala/io/iduce/repo/SQLiteTaskRepositoryImpl.scala`
- Core models: `scala/automata/core/src/main/scala/uk/sky/kurate/package.scala`

