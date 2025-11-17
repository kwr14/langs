# Workflow Engine - Visual Diagrams

This document contains visual diagrams to help understand the workflow engine architecture and execution flow.

## System Architecture Diagram

```mermaid
graph TB
    subgraph "Client Layer"
        CLI[CLI/Scripts]
        SWAGGER[Swagger UI]
        HTTP[HTTP Client]
    end

    subgraph "API Layer - http4s"
        SERVER[ServerComponent<br/>Port 8080]
        ROUTES[HTTP Routes]
        APIDOCS[API Docs<br/>OpenAPI 3.1]
    end

    subgraph "Business Logic Layer"
        ENGINE[WorkflowEngine<br/>Orchestration]
        WORKER[Worker<br/>Task Execution]
        SCHEDULER[Task Scheduler<br/>Dependency Resolution]
    end

    subgraph "Persistence Layer"
        PERSIST[PersistenceLayer<br/>Interface]
        INMEM[InMemoryPersistence<br/>Development]
        CASSANDRA[CassandraPersistence<br/>Production - Future]
    end

    subgraph "Data Models"
        WORKFLOW[Workflow<br/>Runtime State]
        TASK[Task<br/>Execution Unit]
        RESULT[WorkflowResult<br/>Output]
        TRANSITION[Transition<br/>State Changes]
    end

    subgraph "External Systems - Future"
        DB[(Cassandra<br/>Durable Storage)]
        QUEUE[Message Queue<br/>Task Distribution]
        METRICS[Metrics/Monitoring<br/>Observability]
    end

    CLI --> SERVER
    SWAGGER --> SERVER
    HTTP --> SERVER
    
    SERVER --> ROUTES
    SERVER --> APIDOCS
    
    ROUTES --> ENGINE
    ENGINE --> WORKER
    ENGINE --> SCHEDULER
    ENGINE --> PERSIST
    
    PERSIST -.-> INMEM
    PERSIST -.-> CASSANDRA
    
    ENGINE --> WORKFLOW
    ENGINE --> TASK
    ENGINE --> RESULT
    ENGINE --> TRANSITION
    
    CASSANDRA -.Future.-> DB
    WORKER -.Future.-> QUEUE
    ENGINE -.Future.-> METRICS

    style SERVER fill:#4A90E2,color:#fff
    style ENGINE fill:#50C878,color:#fff
    style PERSIST fill:#F39C12,color:#fff
    style CASSANDRA fill:#E74C3C,color:#fff
    style DB fill:#E74C3C,color:#fff
    style QUEUE fill:#9B59B6,color:#fff
    style METRICS fill:#1ABC9C,color:#fff
```

## Workflow Execution Sequence

```mermaid
sequenceDiagram
    participant Client
    participant API as ServerComponent
    participant Engine as WorkflowEngine
    participant Persist as PersistenceLayer
    participant Worker
    
    Client->>API: POST /workflows<br/>{workflowDefinition}
    API->>Engine: startWorkflow(def, vars)
    
    Engine->>Engine: Create Workflow & Tasks
    Engine->>Persist: saveWorkflow(workflow)
    Persist-->>Engine: savedWorkflow
    
    Engine->>Persist: saveTask(task1)
    Engine->>Persist: saveTask(task2)
    Engine->>Persist: saveTask(taskN)
    
    Engine->>Persist: saveTransition(Pending→Running)
    Engine->>Persist: updateWorkflow(status=Running)
    
    Engine-->>API: workflow (Running)
    API-->>Client: 200 OK {workflow}
    
    Note over Engine,Worker: Async Execution (non-blocking)
    
    Engine->>Engine: scheduleNextTasks()
    Engine->>Engine: Find tasks with no dependencies
    
    loop For each ready task
        Engine->>Worker: executeTask(task)
        Worker->>Worker: Perform work
        Worker->>Persist: saveTaskResult(result)
        Worker->>Persist: updateTask(status=Completed)
        Worker-->>Engine: Task completed
        
        Engine->>Persist: updateTask(task, status)
        Engine->>Engine: Check dependent tasks
        Engine->>Engine: scheduleNextTasks()
    end
    
    Engine->>Engine: All tasks completed?
    Engine->>Persist: updateWorkflow(status=Completed)
    Engine->>Persist: saveWorkflowResult(result)
    
    Note over Client,Persist: Client polls for results
    
    Client->>API: GET /workflows/{id}
    API->>Persist: getWorkflow(id)
    Persist-->>API: workflow (Completed)
    API-->>Client: 200 OK {workflow}
    
    Client->>API: GET /workflows/{id}/result
    API->>Persist: getWorkflowResult(id)
    Persist-->>API: workflowResult
    API-->>Client: 200 OK {result}
```

## Task Dependency Graph Example

```mermaid
graph LR
    A[Extract Data] --> B[Validate Data]
    B --> C[Transform Data]
    C --> D[Load to Warehouse]
    D --> E[Update Metadata]
    
    style A fill:#4A90E2,color:#fff
    style B fill:#50C878,color:#fff
    style C fill:#F39C12,color:#fff
    style D fill:#E74C3C,color:#fff
    style E fill:#9B59B6,color:#fff
```

## Parallel Execution Example (Video Processing)

```mermaid
graph TB
    A[Download Video] --> B[Extract Metadata]
    A --> C[Transcode 1080p]
    A --> D[Transcode 720p]
    A --> E[Transcode 480p]
    A --> F[Generate Thumbnail]
    
    B --> G[Upload Processed Files]
    C --> G
    D --> G
    E --> G
    F --> G
    
    G --> H[Update CDN Cache]
    
    style A fill:#4A90E2,color:#fff
    style B fill:#50C878,color:#fff
    style C fill:#F39C12,color:#fff
    style D fill:#F39C12,color:#fff
    style E fill:#F39C12,color:#fff
    style F fill:#9B59B6,color:#fff
    style G fill:#E74C3C,color:#fff
    style H fill:#1ABC9C,color:#fff
```

Note: Tasks C, D, E, F execute in parallel after A completes.

