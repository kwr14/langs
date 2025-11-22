# Workflow Engine - Architecture Guide

## 🏗️ System Architecture

### High-Level Overview

The Workflow Engine is a **durable, distributed task orchestration system** built with functional programming principles using Scala, Cats Effect, and http4s. It provides a REST API for creating and managing workflows with complex task dependencies.

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  Swagger UI  │  │ CLI Scripts  │  │ HTTP Clients │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTP/REST
┌────────────────────────────▼────────────────────────────────────┐
│                         API LAYER (http4s)                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ ServerComponent - Port 8080                               │  │
│  │  • Workflow Routes (CRUD)                                 │  │
│  │  • OpenAPI Documentation                                  │  │
│  │  • Swagger UI Integration                                 │  │
│  │  • JSON Serialization (jsoniter-scala)                    │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                    BUSINESS LOGIC LAYER                          │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ WorkflowEngine - Orchestration                            │  │
│  │  • Workflow Lifecycle Management                          │  │
│  │  • Task Dependency Resolution                             │  │
│  │  • Async Task Scheduling                                  │  │
│  │  • State Transition Management                            │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ Worker - Task Execution                                   │  │
│  │  • Execute individual tasks                               │  │
│  │  • Retry logic with exponential backoff                   │  │
│  │  • Result persistence                                     │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                     PERSISTENCE LAYER                            │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ PersistenceLayer (Trait/Interface)                        │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────┐         ┌──────────────────────────┐    │
│  │ InMemoryPersist  │         │ CassandraPersistence     │    │
│  │ (Development)    │         │ (Production - Future)    │    │
│  └──────────────────┘         └──────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
```

### Core Components

#### 1. **ServerComponent** (API Layer)
- **Technology**: http4s with Ember server
- **Port**: 8080
- **Responsibilities**:
  - HTTP request handling
  - JSON serialization/deserialization
  - OpenAPI documentation serving
  - Swagger UI integration
  - Route management

**Key Routes**:
```scala
POST   /workflows              // Create workflow
GET    /workflows              // List all workflows
GET    /workflows/{id}         // Get workflow status
GET    /workflows/{id}/result  // Get workflow result
GET    /api-docs               // Swagger UI
GET    /api-docs/openapi.yaml  // OpenAPI spec
```

#### 2. **WorkflowEngine** (Orchestration)
- **Responsibilities**:
  - Workflow lifecycle management (create, start, complete)
  - Task dependency graph resolution
  - Asynchronous task scheduling
  - State transition tracking
  - Workflow status updates

**Key Methods**:
```scala
def startWorkflow(workflowDef: WorkflowDefinition, variables: Map[String, String]): Future[Workflow]
def scheduleNextTasks(workflow: Workflow): Future[Unit]
def updateWorkflowStatus(workflowId: ID): Future[Unit]
```

#### 3. **Worker** (Task Execution)
- **Responsibilities**:
  - Execute individual tasks
  - Retry failed tasks with configurable max retries
  - Save task results
  - Update task status

**Key Methods**:
```scala
def executeTask(task: Task): Future[TaskResult]
```

#### 4. **PersistenceLayer** (Data Access)
- **Pattern**: Repository pattern with trait-based abstraction
- **Implementations**:
  - `InMemoryPersistence`: In-memory storage for development/testing
  - `CassandraPersistence`: Durable storage (future implementation)

**Key Operations**:
```scala
def saveWorkflow(workflow: Workflow): Future[Workflow]
def getWorkflow(id: ID): Future[Option[Workflow]]
def listWorkflows(): Future[List[Workflow]]
def updateWorkflow(workflow: Workflow): Future[Workflow]
def saveTask(task: Task): Future[Task]
def updateTask(task: Task): Future[Task]
def saveTaskResult(result: TaskResult): Future[TaskResult]
def getWorkflowResult(workflowId: ID): Future[Option[WorkflowResult]]
def saveTransition(transition: Transition): Future[Transition]
```

### Data Models

#### Core Domain Models

```scala
// Workflow runtime state
case class Workflow(
  id: ID,                    // UUID
  name: String,
  status: Status,            // Pending | Running | Completed | Failed
  tasks: List[Task],
  variables: Map[String, String],
  createdAt: Long,
  updatedAt: Long
)

// Task execution unit
case class Task(
  id: ID,
  name: String,
  status: Status,
  taskType: String,
  parameters: Map[String, String],
  retries: Int,
  maxRetries: Int,
  dependencies: Set[ID],
  createdAt: Long,
  updatedAt: Long
)

// Workflow template
case class WorkflowDefinition(
  name: String,
  taskDefinitions: List[TaskDefinition]
)

// Task template
case class TaskDefinition(
  name: String,
  taskType: String,
  parameterTypes: Map[String, String],
  maxRetries: Int,
  dependencies: Set[String]  // Task names
)
```

### Execution Flow

See the **Workflow Execution Flow** diagram above for detailed sequence.

**Key Points**:
1. **Non-blocking Creation**: Workflow creation returns immediately with status "Running"
2. **Async Execution**: Tasks execute in background using Scala Futures
3. **Dependency Resolution**: Engine schedules tasks only when dependencies are met
4. **State Synchronization**: Task updates propagate to workflow state
5. **Result Persistence**: Results saved separately for efficient retrieval

### Technology Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| HTTP Server | http4s + Ember | Functional HTTP server |
| Effects | Cats Effect 3.5+ | Pure functional effects (IO, Async) |
| JSON | jsoniter-scala 2.30 | High-performance JSON codec generation |
| Concurrency | Scala Futures | Async task execution |
| Database (Future) | Cassandra 4.17 | Durable, distributed storage |
| API Docs | OpenAPI 3.1 + Swagger UI | Interactive documentation |

### Design Patterns

1. **Repository Pattern**: `PersistenceLayer` trait with multiple implementations
2. **Dependency Injection**: Constructor-based DI for testability
3. **Functional Core, Imperative Shell**: Pure business logic with IO at edges
4. **Fire-and-Forget**: Async task execution doesn't block API responses
5. **State Machine**: Explicit status transitions (Pending → Running → Completed/Failed)

### Scalability Considerations

**Current (In-Memory)**:
- Single-node execution
- Limited by JVM heap
- No durability across restarts

**Future (Cassandra + Distributed)**:
- Multi-node horizontal scaling
- Durable state persistence
- Distributed task execution
- Event sourcing with transition log

