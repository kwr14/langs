# Workflow Engine - Enhancement Guide

## 🎯 Roadmap for Production-Ready System

This guide outlines how to enhance the current workflow engine into a production-grade distributed system.

---

## Phase 1: Cassandra Integration (Durable Persistence)

### Current State
- In-memory persistence (data lost on restart)
- Single-node execution
- No durability guarantees

### Enhancement: Cassandra Persistence Layer

**1. Schema Design**

```cql
-- Workflows table
CREATE TABLE workflows (
    id UUID PRIMARY KEY,
    name TEXT,
    status TEXT,
    variables MAP<TEXT, TEXT>,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Tasks table
CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    workflow_id UUID,
    name TEXT,
    status TEXT,
    task_type TEXT,
    parameters MAP<TEXT, TEXT>,
    retries INT,
    max_retries INT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Task dependencies
CREATE TABLE task_dependencies (
    task_id UUID,
    depends_on_task_id UUID,
    PRIMARY KEY (task_id, depends_on_task_id)
);

-- Task results
CREATE TABLE task_results (
    task_id UUID PRIMARY KEY,
    output TEXT,
    error TEXT,
    completed_at TIMESTAMP
);

-- Workflow results
CREATE TABLE workflow_results (
    workflow_id UUID PRIMARY KEY,
    error TEXT,
    completed_at TIMESTAMP
);

-- State transitions (event sourcing)
CREATE TABLE transitions (
    entity_id UUID,
    timestamp TIMESTAMP,
    from_status TEXT,
    to_status TEXT,
    PRIMARY KEY (entity_id, timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);

-- Workflow tasks index (for querying tasks by workflow)
CREATE TABLE workflow_tasks (
    workflow_id UUID,
    task_id UUID,
    PRIMARY KEY (workflow_id, task_id)
);
```

**2. Implementation**

```scala
class CassandraPersistence(session: CqlSession)(implicit ec: ExecutionContext) 
  extends PersistenceLayer {
  
  override def saveWorkflow(workflow: Workflow): Future[Workflow] = {
    val statement = session.prepare(
      "INSERT INTO workflows (id, name, status, variables, created_at, updated_at) " +
      "VALUES (?, ?, ?, ?, ?, ?)"
    )
    // Execute async and return workflow
  }
  
  override def getWorkflow(id: ID): Future[Option[Workflow]] = {
    val statement = session.prepare("SELECT * FROM workflows WHERE id = ?")
    // Execute and map to Workflow
  }
  
  // Implement other methods...
}
```

**3. Benefits**
- ✅ Durable storage (survives restarts)
- ✅ Distributed architecture
- ✅ Event sourcing via transitions table
- ✅ Scalable to millions of workflows

---

## Phase 2: Distributed Task Execution

### Current State
- Tasks execute on same node as API
- Limited by single-node resources
- No horizontal scaling

### Enhancement: Message Queue + Worker Pool

**1. Architecture**

```
API Server → Kafka/RabbitMQ → Worker Pool (N nodes)
```

**2. Implementation**

```scala
// Producer (in WorkflowEngine)
class TaskProducer(kafkaProducer: KafkaProducer[String, Task]) {
  def scheduleTask(task: Task): Future[Unit] = {
    val record = new ProducerRecord("workflow-tasks", task.id.toString, task)
    Future {
      kafkaProducer.send(record).get()
    }
  }
}

// Consumer (Worker nodes)
class TaskConsumer(
  kafkaConsumer: KafkaConsumer[String, Task],
  worker: Worker,
  persistence: PersistenceLayer
) {
  def start(): Unit = {
    kafkaConsumer.subscribe(Collections.singletonList("workflow-tasks"))
    
    while (true) {
      val records = kafkaConsumer.poll(Duration.ofMillis(100))
      records.forEach { record =>
        val task = record.value()
        worker.executeTask(task).foreach { result =>
          persistence.saveTaskResult(result)
          persistence.updateTask(task.copy(status = Completed))
        }
      }
    }
  }
}
```

**3. Benefits**
- ✅ Horizontal scaling (add more workers)
- ✅ Load balancing across workers
- ✅ Fault tolerance (dead letter queues)
- ✅ Resource isolation (CPU-intensive tasks on dedicated nodes)

---

## Phase 3: Advanced Scheduling & Retry

### Enhancement: Exponential Backoff & Circuit Breaker

**1. Exponential Backoff**

```scala
class SmartWorker(
  persistence: PersistenceLayer,
  maxBackoffSeconds: Int = 3600
)(implicit ec: ExecutionContext) extends Worker {
  
  override def executeTask(task: Task): Future[TaskResult] = {
    def attemptWithBackoff(attempt: Int): Future[TaskResult] = {
      executeTaskLogic(task).recoverWith {
        case ex if attempt < task.maxRetries =>
          val backoffSeconds = Math.min(
            Math.pow(2, attempt).toInt,
            maxBackoffSeconds
          )
          
          println(s"Task ${task.id} failed, retrying in ${backoffSeconds}s")
          
          Thread.sleep(backoffSeconds * 1000)
          
          persistence.updateTask(
            task.copy(retries = attempt + 1)
          ).flatMap(_ => attemptWithBackoff(attempt + 1))
          
        case ex =>
          Future.failed(ex)
      }
    }
    
    attemptWithBackoff(task.retries)
  }
}
```

**2. Circuit Breaker**

```scala
import akka.pattern.CircuitBreaker

class ResilientWorker(
  circuitBreaker: CircuitBreaker
)(implicit ec: ExecutionContext) extends Worker {
  
  override def executeTask(task: Task): Future[TaskResult] = {
    circuitBreaker.withCircuitBreaker {
      executeTaskLogic(task)
    }
  }
}
```

---

## Phase 4: Monitoring & Observability

### Enhancement: Metrics, Logging, Tracing

**1. Prometheus Metrics**

```scala
import io.prometheus.client.{Counter, Histogram, Gauge}

object WorkflowMetrics {
  val workflowsCreated = Counter.build()
    .name("workflows_created_total")
    .help("Total workflows created")
    .register()
  
  val workflowsCompleted = Counter.build()
    .name("workflows_completed_total")
    .help("Total workflows completed")
    .register()
  
  val workflowDuration = Histogram.build()
    .name("workflow_duration_seconds")
    .help("Workflow execution duration")
    .register()
  
  val activeWorkflows = Gauge.build()
    .name("active_workflows")
    .help("Currently running workflows")
    .register()
  
  val taskExecutionTime = Histogram.build()
    .name("task_execution_seconds")
    .help("Task execution time")
    .labelNames("task_type")
    .register()
}

// Usage in WorkflowEngine
def startWorkflow(...): Future[Workflow] = {
  WorkflowMetrics.workflowsCreated.inc()
  WorkflowMetrics.activeWorkflows.inc()
  
  val timer = WorkflowMetrics.workflowDuration.startTimer()
  
  // ... workflow execution ...
  
  result.onComplete {
    case Success(_) =>
      WorkflowMetrics.workflowsCompleted.inc()
      WorkflowMetrics.activeWorkflows.dec()
      timer.observeDuration()
    case Failure(_) =>
      WorkflowMetrics.activeWorkflows.dec()
  }
  
  result
}
```

**2. Structured Logging**

```scala
import org.slf4j.LoggerFactory
import net.logstash.logback.argument.StructuredArguments._

class WorkflowEngine(...) {
  private val logger = LoggerFactory.getLogger(getClass)
  
  def startWorkflow(...): Future[Workflow] = {
    logger.info(
      "Starting workflow",
      keyValue("workflowId", workflow.id),
      keyValue("workflowName", workflow.name),
      keyValue("taskCount", workflow.tasks.size)
    )
    
    // ... execution ...
  }
}
```

**3. Distributed Tracing (OpenTelemetry)**

```scala
import io.opentelemetry.api.trace.{Span, Tracer}

class TracedWorkflowEngine(
  tracer: Tracer,
  persistence: PersistenceLayer
)(implicit ec: ExecutionContext) extends WorkflowEngine(persistence) {
  
  override def startWorkflow(...): Future[Workflow] = {
    val span = tracer.spanBuilder("workflow.start")
      .setAttribute("workflow.name", workflowDef.name)
      .startSpan()
    
    try {
      super.startWorkflow(workflowDef, variables).andThen {
        case Success(workflow) =>
          span.setAttribute("workflow.id", workflow.id.toString)
          span.end()
        case Failure(ex) =>
          span.recordException(ex)
          span.end()
      }
    } catch {
      case ex: Throwable =>
        span.recordException(ex)
        span.end()
        throw ex
    }
  }
}
```

---

## Phase 5: Advanced Features

### 1. Conditional Branching

```scala
case class ConditionalTask(
  condition: String,  // e.g., "task1.output == 'success'"
  ifTrue: List[TaskDefinition],
  ifFalse: List[TaskDefinition]
)
```

### 2. Parallel Execution Limits

```scala
case class WorkflowDefinition(
  name: String,
  taskDefinitions: List[TaskDefinition],
  maxParallelTasks: Int = 10  // Limit concurrent tasks
)
```

### 3. Workflow Versioning

```scala
case class Workflow(
  // ... existing fields ...
  version: Int = 1,
  definitionVersion: String = "v1.0.0"
)
```

### 4. Scheduled Workflows (Cron)

```scala
case class ScheduledWorkflow(
  workflowDefinition: WorkflowDefinition,
  cronExpression: String,  // "0 0 * * *" (daily at midnight)
  enabled: Boolean = true
)
```

### 5. Workflow Cancellation

```scala
def cancelWorkflow(workflowId: ID): Future[Unit] = {
  for {
    workflow <- persistence.getWorkflow(workflowId)
    _ <- persistence.updateWorkflow(
      workflow.copy(status = Failed)
    )
    _ <- Future.sequence(
      workflow.tasks
        .filter(_.status == Running)
        .map(t => persistence.updateTask(t.copy(status = Failed)))
    )
  } yield ()
}
```

---

## Summary: Production Readiness Checklist

- [ ] **Phase 1**: Cassandra persistence (durability)
- [ ] **Phase 2**: Message queue + worker pool (scalability)
- [ ] **Phase 3**: Smart retry + circuit breaker (resilience)
- [ ] **Phase 4**: Metrics + logging + tracing (observability)
- [ ] **Phase 5**: Advanced features (flexibility)

Each phase builds on the previous, creating a robust, scalable, production-ready workflow orchestration system.

