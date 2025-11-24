package uk.sky.kurate.persistence

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach
import core._
import java.util.UUID
import java.nio.file.{Files, Paths}
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

class SQLitePersistenceSpec extends AnyFlatSpec with Matchers with BeforeAndAfterEach {
  implicit val ec: ExecutionContext = ExecutionContext.global

  val testDbPath = "test-workflows.db"
  var persistence: SQLitePersistence = _

  override def beforeEach(): Unit = {
    // Delete test database if it exists
    try {
      Files.deleteIfExists(Paths.get(testDbPath))
      Files.deleteIfExists(Paths.get(s"$testDbPath-shm"))
      Files.deleteIfExists(Paths.get(s"$testDbPath-wal"))
    } catch {
      case _: Exception => // Ignore
    }
    persistence = SQLitePersistence(testDbPath)
    Thread.sleep(100) // Give time for schema creation
  }

  override def afterEach(): Unit = {
    // Clean up test database
    try {
      Files.deleteIfExists(Paths.get(testDbPath))
      Files.deleteIfExists(Paths.get(s"$testDbPath-shm"))
      Files.deleteIfExists(Paths.get(s"$testDbPath-wal"))
    } catch {
      case _: Exception => // Ignore
    }
  }

  "SQLitePersistence" should "save and retrieve a workflow" in {
    val task1 = Task(
      name = "Task 1",
      taskType = "test",
      parameters = Map("key" -> "value"),
      dependencies = Set.empty
    )

    val workflow = Workflow(
      name = "Test Workflow",
      tasks = List(task1),
      variables = Map("env" -> "test")
    )

    val savedFuture = persistence.saveWorkflow(workflow)
    val saved = Await.result(savedFuture, 5.seconds)
    saved.id shouldBe workflow.id

    val retrievedFuture = persistence.getWorkflow(workflow.id)
    val retrieved = Await.result(retrievedFuture, 5.seconds)
    retrieved shouldBe defined
    retrieved.get.name shouldBe "Test Workflow"
    retrieved.get.tasks.size shouldBe 1
    retrieved.get.tasks.head.name shouldBe "Task 1"
  }

  it should "update a workflow" in {
    val workflow = Workflow(
      name = "Original Name",
      tasks = List.empty,
      variables = Map.empty
    )

    Await.result(persistence.saveWorkflow(workflow), 5.seconds)

    val updated = workflow.copy(name = "Updated Name", status = Running)
    Await.result(persistence.updateWorkflow(updated), 5.seconds)

    val retrieved = Await.result(persistence.getWorkflow(workflow.id), 5.seconds)
    retrieved shouldBe defined
    retrieved.get.name shouldBe "Updated Name"
    retrieved.get.status shouldBe Running
  }

  it should "list all workflows" in {
    val workflow1 = Workflow(name = "Workflow 1", tasks = List.empty, variables = Map.empty)
    val workflow2 = Workflow(name = "Workflow 2", tasks = List.empty, variables = Map.empty)

    Await.result(persistence.saveWorkflow(workflow1), 5.seconds)
    Await.result(persistence.saveWorkflow(workflow2), 5.seconds)

    val workflows = Await.result(persistence.listWorkflows(), 5.seconds)
    workflows.size shouldBe 2
    workflows.map(_.name) should contain allOf ("Workflow 1", "Workflow 2")
  }

  it should "delete a workflow" in {
    val workflow = Workflow(name = "To Delete", tasks = List.empty, variables = Map.empty)
    Await.result(persistence.saveWorkflow(workflow), 5.seconds)

    val deleted = Await.result(persistence.deleteWorkflow(workflow.id), 5.seconds)
    deleted shouldBe true

    val retrieved = Await.result(persistence.getWorkflow(workflow.id), 5.seconds)
    retrieved shouldBe None
  }

  it should "save and retrieve task results" in {
    val taskId = UUID.randomUUID()
    val result = TaskResult(taskId, "Success output", None)

    Await.result(persistence.saveTaskResult(result), 5.seconds)

    val retrieved = Await.result(persistence.getTaskResult(taskId), 5.seconds)
    retrieved shouldBe defined
    retrieved.get.output shouldBe "Success output"
    retrieved.get.error shouldBe None
  }

  it should "save and retrieve workflow results" in {
    val workflowId = UUID.randomUUID()
    val taskId = UUID.randomUUID()
    val taskResult = TaskResult(taskId, "Task output", None)
    val workflowResult = WorkflowResult(workflowId, Map(taskId -> taskResult), None)

    Await.result(persistence.saveWorkflowResult(workflowResult), 5.seconds)

    val retrieved = Await.result(persistence.getWorkflowResult(workflowId), 5.seconds)
    retrieved shouldBe defined
    retrieved.get.taskResults.size shouldBe 1
    retrieved.get.taskResults(taskId).output shouldBe "Task output"
  }

  it should "save and retrieve transitions" in {
    val entityId = UUID.randomUUID()
    val transition1 = Transition(entityId, Pending, Running)
    val transition2 = Transition(entityId, Running, Completed)

    Await.result(persistence.saveTransition(transition1), 5.seconds)
    Thread.sleep(10) // Ensure different timestamps
    Await.result(persistence.saveTransition(transition2), 5.seconds)

    val transitions = Await.result(persistence.getTransitions(entityId), 5.seconds)
    transitions.size shouldBe 2
    transitions.head.fromStatus shouldBe Pending
    transitions.head.toStatus shouldBe Running
    transitions(1).fromStatus shouldBe Running
    transitions(1).toStatus shouldBe Completed
  }
}

