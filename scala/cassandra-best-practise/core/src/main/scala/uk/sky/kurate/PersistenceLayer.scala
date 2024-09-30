package uk.sky.kurate

import core._

import scala.concurrent.Future
import scala.collection.mutable


// Persistence trait defining the operations
trait PersistenceLayer {
  def saveWorkflow(workflow: Workflow): Future[Workflow]
  def getWorkflow(id: ID): Future[Option[Workflow]]
  def updateWorkflow(workflow: Workflow): Future[Workflow]
  def deleteWorkflow(id: ID): Future[Boolean]

  def saveTask(task: Task): Future[Task]
  def getTask(id: ID): Future[Option[Task]]
  def updateTask(task: Task): Future[Task]
  def deleteTask(id: ID): Future[Boolean]

  def saveWorkflowResult(result: WorkflowResult): Future[WorkflowResult]
  def getWorkflowResult(workflowId: ID): Future[Option[WorkflowResult]]

  def saveTaskResult(result: TaskResult): Future[TaskResult]
  def getTaskResult(taskId: ID): Future[Option[TaskResult]]

  def saveTransition(transition: Transition): Future[Transition]
  def getTransitions(entityId: ID): Future[List[Transition]]
}

// In-memory implementation of the PersistenceLayer
class InMemoryPersistence extends PersistenceLayer {
  private val workflows = mutable.Map[ID, Workflow]()
  private val tasks = mutable.Map[ID, Task]()
  private val workflowResults = mutable.Map[ID, WorkflowResult]()
  private val taskResults = mutable.Map[ID, TaskResult]()
  private val transitions = mutable.Map[ID, List[Transition]]()

  import scala.concurrent.ExecutionContext.Implicits.global

  override def saveWorkflow(workflow: Workflow): Future[Workflow] = Future {
    workflows.put(workflow.id, workflow)
    workflow
  }

  override def getWorkflow(id: ID): Future[Option[Workflow]] = Future {
    workflows.get(id)
  }

  override def updateWorkflow(workflow: Workflow): Future[Workflow] = Future {
    workflows.update(workflow.id, workflow)
    workflow
  }

  override def deleteWorkflow(id: ID): Future[Boolean] = Future {
    workflows.remove(id).isDefined
  }

  override def saveTask(task: Task): Future[Task] = Future {
    tasks.put(task.id, task)
    task
  }

  override def getTask(id: ID): Future[Option[Task]] = Future {
    tasks.get(id)
  }

  override def updateTask(task: Task): Future[Task] = Future {
    tasks.update(task.id, task)
    task
  }

  override def deleteTask(id: ID): Future[Boolean] = Future {
    tasks.remove(id).isDefined
  }

  override def saveWorkflowResult(result: WorkflowResult): Future[WorkflowResult] = Future {
    workflowResults.put(result.workflowId, result)
    result
  }

  override def getWorkflowResult(workflowId: ID): Future[Option[WorkflowResult]] = Future {
    workflowResults.get(workflowId)
  }

  override def saveTaskResult(result: TaskResult): Future[TaskResult] = Future {
    taskResults.put(result.taskId, result)
    result
  }

  override def getTaskResult(taskId: ID): Future[Option[TaskResult]] = Future {
    taskResults.get(taskId)
  }

  override def saveTransition(transition: Transition): Future[Transition] = Future {
    val currentTransitions = transitions.getOrElse(transition.entityId, List.empty)
    transitions.update(transition.entityId, transition :: currentTransitions)
    transition
  }

  override def getTransitions(entityId: ID): Future[List[Transition]] = Future {
    transitions.getOrElse(entityId, List.empty)
  }
}
