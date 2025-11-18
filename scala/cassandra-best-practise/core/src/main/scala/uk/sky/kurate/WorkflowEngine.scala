package uk.sky.kurate

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import core._
import java.util.UUID

class WorkflowEngine(persistenceLayer: PersistenceLayer)(implicit
    ec: ExecutionContext
) {

  def startWorkflow(
      workflowDef: WorkflowDefinition,
      variables: Map[String, String]
  ): Future[Workflow] = {
    // First create tasks without dependencies
    val tasksWithoutDeps: List[Task] = workflowDef.taskDefinitions.map {
      taskDef =>
        Task(
          name = taskDef.name,
          taskType = taskDef.taskType,
          parameters = Map.empty,
          maxRetries = taskDef.maxRetries,
          dependencies = Set.empty // Will be filled in next step
        )
    }

    // Build name-to-ID mapping
    val nameToId: Map[String, ID] =
      tasksWithoutDeps.map(t => t.name -> t.id).toMap

    // Now resolve dependencies by name
    val tasks: List[Task] =
      workflowDef.taskDefinitions.zip(tasksWithoutDeps).map {
        case (taskDef, task) =>
          val resolvedDeps: Set[ID] =
            taskDef.dependencies.flatMap(depName => nameToId.get(depName)).toSet
          task.copy(dependencies = resolvedDeps)
      }

    val workflow = Workflow(
      name = workflowDef.name,
      tasks = tasks,
      variables = variables
    )

    val result = for {
      savedWorkflow <- persistenceLayer.saveWorkflow(workflow)
      // Save all tasks individually
      _ <- Future.sequence(savedWorkflow.tasks.map(persistenceLayer.saveTask))
      _ <- persistenceLayer.saveTransition(
        Transition(savedWorkflow.id, Pending, Running)
      )
      updatedWorkflow <- persistenceLayer.updateWorkflow(
        savedWorkflow.copy(status = Running)
      )
    } yield updatedWorkflow

    // Schedule tasks asynchronously after returning the workflow
    result.foreach(w => scheduleNextTasks(w))

    result
  }

  private def scheduleNextTasks(workflow: Workflow): Future[Unit] = {
    val readyTasks = workflow.tasks.filter(canTaskStart(_, workflow))
    Future.sequence(readyTasks.map(executeTask(_, workflow))).map(_ => ())
  }

  private def canTaskStart(task: Task, workflow: Workflow): Boolean = {
    task.status == Pending &&
    task.dependencies.forall(depId =>
      workflow.tasks.find(_.id == depId).exists(_.status == Completed)
    )
  }

  private def executeTask(task: Task, workflow: Workflow): Future[Unit] = {
    for {
      _ <- persistenceLayer.saveTransition(
        Transition(task.id, task.status, Running)
      )
      updatedTask <- persistenceLayer.updateTask(task.copy(status = Running))
      result <- runTask(
        updatedTask,
        workflow
      ) // This would call out to your task execution system
      _ <- handleTaskResult(result, workflow)
    } yield ()
  }

  private def runTask(task: Task, workflow: Workflow): Future[TaskResult] = {
    // In a real implementation, this would dispatch the task to a worker
    // For now, we'll just simulate task execution
    Future {
      Thread.sleep(1000) // Simulate work
      TaskResult(task.id, s"Result of ${task.name}")
    }
  }

  private def handleTaskResult(
      result: TaskResult,
      workflow: Workflow
  ): Future[Unit] = {
    for {
      _ <- persistenceLayer.saveTaskResult(result)
      task <- persistenceLayer.getTask(result.taskId).map(_.get)
      updatedTask = task.copy(status = Completed)
      _ <- persistenceLayer.saveTransition(
        Transition(task.id, Running, Completed)
      )
      _ <- persistenceLayer.updateTask(updatedTask)
      updatedWorkflow <- updateWorkflowStatus(workflow)
      _ <-
        if (updatedWorkflow.status == Completed)
          completeWorkflow(updatedWorkflow)
        else scheduleNextTasks(updatedWorkflow)
    } yield ()
  }

  private def updateWorkflowStatus(workflow: Workflow): Future[Workflow] = {
    persistenceLayer.getWorkflow(workflow.id).flatMap {
      case Some(currentWorkflow) =>
        val allTasksCompleted =
          currentWorkflow.tasks.forall(_.status == Completed)
        if (allTasksCompleted && currentWorkflow.status != Completed) {
          val updatedWorkflow = currentWorkflow.copy(status = Completed)
          persistenceLayer
            .saveTransition(
              Transition(workflow.id, currentWorkflow.status, Completed)
            )
            .flatMap(_ => persistenceLayer.updateWorkflow(updatedWorkflow))
        } else {
          Future.successful(currentWorkflow)
        }
      case None =>
        Future.failed(new Exception(s"Workflow ${workflow.id} not found"))
    }
  }

  private def completeWorkflow(workflow: Workflow): Future[Unit] = {
    for {
      taskResults <- Future.sequence(
        workflow.tasks.map(task => persistenceLayer.getTaskResult(task.id))
      )
      workflowResult = WorkflowResult(
        workflow.id,
        taskResults.flatten.map(r => r.taskId -> r).toMap
      )
      _ <- persistenceLayer.saveWorkflowResult(workflowResult)
    } yield ()
  }
}
