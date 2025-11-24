package uk.sky.kurate.examples

import uk.sky.kurate.persistence.SQLitePersistence
import uk.sky.kurate.{WorkflowEngine, PersistenceLayer}
import core._
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import java.util.UUID

/**
 * Example demonstrating SQLite-based persistence and workflow recovery.
 */
object PersistenceExample {
  implicit val ec: ExecutionContext = ExecutionContext.global

  def main(args: Array[String]): Unit = {
    val dbPath = "workflows.db"
    val persistence: PersistenceLayer = SQLitePersistence(dbPath)

    println("=" * 60)
    println("SQLite Persistence Example")
    println("=" * 60)

    // Example 1: Create and save a workflow
    println("\n1. Creating and saving a workflow...")
    val workflowDef = WorkflowDefinition(
      name = "Data Processing Pipeline",
      taskDefinitions = List(
        TaskDefinition("Extract Data", "extract", Map.empty, 3, Set.empty),
        TaskDefinition("Transform Data", "transform", Map.empty, 3, Set("Extract Data")),
        TaskDefinition("Load Data", "load", Map.empty, 3, Set("Transform Data"))
      )
    )

    val engine = new WorkflowEngine(persistence)
    val workflowFuture = engine.startWorkflow(workflowDef, Map("env" -> "production"))
    val workflow = Await.result(workflowFuture, 5.seconds)

    println(s"✓ Workflow created: ${workflow.id}")
    println(s"  Name: ${workflow.name}")
    println(s"  Status: ${workflow.status}")
    println(s"  Tasks: ${workflow.tasks.size}")

    // Example 2: Retrieve the workflow
    println("\n2. Retrieving workflow from database...")
    val retrievedFuture = persistence.getWorkflow(workflow.id)
    val retrieved = Await.result(retrievedFuture, 5.seconds)

    retrieved match {
      case Some(wf) =>
        println(s"✓ Workflow retrieved: ${wf.id}")
        println(s"  Status: ${wf.status}")
        println(s"  Tasks:")
        wf.tasks.foreach { task =>
          println(s"    - ${task.name} (${task.status})")
        }
      case None =>
        println("✗ Workflow not found!")
    }

    // Example 3: Query state transitions (audit trail)
    println("\n3. Querying state transitions (audit trail)...")
    val transitionsFuture = persistence.getTransitions(workflow.id)
    val transitions = Await.result(transitionsFuture, 5.seconds)

    println(s"✓ Found ${transitions.size} transitions:")
    transitions.foreach { t =>
      val timestamp = new java.util.Date(t.timestamp)
      println(s"  ${t.fromStatus} → ${t.toStatus} at $timestamp")
    }

    // Example 4: List all workflows
    println("\n4. Listing all workflows in database...")
    val allWorkflowsFuture = persistence.listWorkflows()
    val allWorkflows = Await.result(allWorkflowsFuture, 5.seconds)

    println(s"✓ Found ${allWorkflows.size} workflow(s):")
    allWorkflows.foreach { wf =>
      println(s"  - ${wf.name} (${wf.id}) - Status: ${wf.status}")
    }

    // Example 5: Simulate workflow recovery
    println("\n5. Simulating workflow recovery after restart...")
    println("  (In a real scenario, the process would restart here)")

    val recoveredFuture = persistence.getWorkflow(workflow.id)
    val recovered = Await.result(recoveredFuture, 5.seconds)

    recovered match {
      case Some(wf) =>
        println(s"✓ Workflow recovered successfully!")
        println(s"  ID: ${wf.id}")
        println(s"  Status: ${wf.status}")
        println(s"  Can resume execution: ${wf.status != Completed}")

        // Show which tasks are completed and which are pending
        val completedTasks = wf.tasks.filter(_.status == Completed)
        val pendingTasks = wf.tasks.filter(_.status == Pending)

        println(s"\n  Completed tasks: ${completedTasks.size}")
        completedTasks.foreach(t => println(s"    ✓ ${t.name}"))

        println(s"\n  Pending tasks: ${pendingTasks.size}")
        pendingTasks.foreach(t => println(s"    ○ ${t.name}"))

      case None =>
        println("✗ Failed to recover workflow!")
    }

    // Example 6: Save and retrieve task results
    println("\n6. Saving and retrieving task results...")
    val taskId = workflow.tasks.head.id
    val taskResult = TaskResult(taskId, "Extracted 1000 records", None)

    Await.result(persistence.saveTaskResult(taskResult), 5.seconds)
    println(s"✓ Task result saved for task: ${workflow.tasks.head.name}")

    val retrievedResultFuture = persistence.getTaskResult(taskId)
    val retrievedResult = Await.result(retrievedResultFuture, 5.seconds)

    retrievedResult match {
      case Some(result) =>
        println(s"✓ Task result retrieved:")
        println(s"  Output: ${result.output}")
        println(s"  Error: ${result.error.getOrElse("None")}")
      case None =>
        println("✗ Task result not found!")
    }

    println("\n" + "=" * 60)
    println("Example completed successfully!")
    println(s"Database location: $dbPath")
    println("=" * 60)
  }
}

