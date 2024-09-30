package object core {
    import java.util.UUID

// Unique identifier for workflows and tasks
type ID = UUID

// Status for both workflows and tasks
sealed trait Status
case object Pending extends Status
case object Running extends Status
case object Completed extends Status
case object Failed extends Status

// Base trait for workflow and task data
trait Metadata {
  def id: ID
  def name: String
  def status: Status
  def createdAt: Long
  def updatedAt: Long
}

// Task definition
case class Task(
  id: ID = UUID.randomUUID(),
  name: String,
  status: Status = Pending,
  createdAt: Long = System.currentTimeMillis(),
  updatedAt: Long = System.currentTimeMillis(),
  taskType: String,
  parameters: Map[String, Any],
  retries: Int = 0,
  maxRetries: Int = 3,
  dependencies: Set[ID] = Set.empty
) extends Metadata

// Workflow definition
case class Workflow(
  id: ID = UUID.randomUUID(),
  name: String,
  status: Status = Pending,
  createdAt: Long = System.currentTimeMillis(),
  updatedAt: Long = System.currentTimeMillis(),
  tasks: List[Task],
  variables: Map[String, Any] = Map.empty
) extends Metadata

// Task result
case class TaskResult(
  taskId: ID,
  output: Any,
  error: Option[String] = None
)

// Workflow result
case class WorkflowResult(
  workflowId: ID,
  taskResults: Map[ID, TaskResult],
  error: Option[String] = None
)

// Transition represents a change in workflow or task state
case class Transition(
  entityId: ID,
  fromStatus: Status,
  toStatus: Status,
  timestamp: Long = System.currentTimeMillis()
)

// WorkflowDefinition for creating workflow templates
case class WorkflowDefinition(
  name: String,
  taskDefinitions: List[TaskDefinition]
)

// TaskDefinition for creating task templates
case class TaskDefinition(
  name: String,
  taskType: String,
  parameterTypes: Map[String, Class[_]],
  maxRetries: Int = 3,
  dependencies: Set[String] = Set.empty
)

}
