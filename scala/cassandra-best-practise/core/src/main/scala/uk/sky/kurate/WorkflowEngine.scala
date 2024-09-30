package uk.sky.kurate

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import core._

class WorkflowEngine(persistenceLayer: PersistenceLayer)(implicit ec: ExecutionContext) {

  def startWorkflow(workflowDef: WorkflowDefinition, variables: Map[String, Any]): Future[Workflow] = {
    val tasks = workflowDef.taskDefinitions.map(createTask)
    val workflow = Workflow(
      name = workflowDef.name,
      tasks = tasks,
      variables = variables
    )

    for {
      savedWorkflow <- persistenceLayer.saveWorkflow(workflow)
      _ <- persistenceLayer.saveTransition(Transition(savedWorkflow.id, Pending, Running))
      updatedWorkflow <- persistenceLayer.updateWorkflow(savedWorkflow.copy(status = Running))
      _ <- scheduleNextTasks(updatedWorkflow)
    } yield updatedWorkflow
  }

  private def createTask(taskDef: TaskDefinition): Task = {
    Task(
      name = taskDef.name,
      taskType = taskDef.taskType,
      parameters = Map.empty, // To be filled when the task is ready to run
      maxRetries = taskDef.maxRetries,
      dependencies = taskDef.dependencies.map(_ => UUID.randomUUID()) // Placeholder IDs
    )
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
      _ <- persistenceLayer.saveTransition(Transition(task.id, task.status, Running))
      updatedTask <- persistenceLayer.updateTask(task.copy(status = Running))
      result <- runTask(updatedTask, workflow) // This would call out to your task execution system
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

  private def handleTaskResult(result: TaskResult, workflow: Workflow): Future[Unit] = {
    for {
      _ <- persistenceLayer.saveTaskResult(result)
      task <- persistenceLayer.getTask(result.taskId).map(_.get)
      updatedTask = task.copy(status = Completed)
      _ <- persistenceLayer.saveTransition(Transition(task.id, Running, Completed))
      _ <- persistenceLayer.updateTask(updatedTask)
      updatedWorkflow <- updateWorkflowStatus(workflow)
      _ <- if (updatedWorkflow.status == Completed) completeWorkflow(updatedWorkflow)
           else scheduleNextTasks(updatedWorkflow)
    } yield ()
  }

  private def updateWorkflowStatus(workflow: Workflow): Future[Workflow] = {
    persistenceLayer.getWorkflow(workflow.id).flatMap {
      case Some(currentWorkflow) =>
        val allTasksCompleted = currentWorkflow.tasks.forall(_.status == Completed)
        if (allTasksCompleted && currentWorkflow.status != Completed) {
          val updatedWorkflow = currentWorkflow.copy(status = Completed)
          persistenceLayer.saveTransition(Transition(workflow.id, currentWorkflow.status, Completed))
            .flatMap(_ => persistenceLayer.updateWorkflow(updatedWorkflow))
        } else {
          Future.successful(currentWorkflow)
        }
      case None => Future.failed(new Exception(s"Workflow ${workflow.id} not found"))
    }
  }

  private def completeWorkflow(workflow: Workflow): Future[Unit] = {
    for {
      taskResults <- Future.sequence(workflow.tasks.map(task => persistenceLayer.getTaskResult(task.id)))
      workflowResult = WorkflowResult(workflow.id, taskResults.flatten.map(r => r.taskId -> r).toMap)
      _ <- persistenceLayer.saveWorkflowResult(workflowResult)
    } yield ()
  }
}
