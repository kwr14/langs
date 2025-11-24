package uk.sky.kurate.persistence

import uk.sky.kurate.PersistenceLayer
import core._
import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.mutable.ListBuffer
import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import java.util.UUID

/** SQLite-based implementation of PersistenceLayer. Provides durable storage
  * for workflows, tasks, and results.
  */
class SQLitePersistence(dbPath: String)(implicit ec: ExecutionContext)
    extends PersistenceLayer {

  // JSON codecs for serialization
  implicit val uuidCodec: JsonValueCodec[UUID] = new JsonValueCodec[UUID] {
    override def decodeValue(in: JsonReader, default: UUID): UUID =
      UUID.fromString(in.readString(null))
    override def encodeValue(x: UUID, out: JsonWriter): Unit =
      out.writeVal(x.toString)
    override def nullValue: UUID = null
  }

  implicit val statusCodec: JsonValueCodec[Status] =
    new JsonValueCodec[Status] {
      override def decodeValue(in: JsonReader, default: Status): Status =
        in.readString(null) match {
          case "Pending"   => Pending
          case "Running"   => Running
          case "Completed" => Completed
          case "Failed"    => Failed
          case other =>
            throw new IllegalArgumentException(s"Unknown status: $other")
        }
      override def encodeValue(x: Status, out: JsonWriter): Unit =
        out.writeVal(x.toString)
      override def nullValue: Status = null
    }

  implicit val uuidSetCodec: JsonValueCodec[Set[UUID]] = JsonCodecMaker.make
  implicit val stringMapCodec: JsonValueCodec[Map[String, String]] =
    JsonCodecMaker.make
  implicit val taskListCodec: JsonValueCodec[List[Task]] = JsonCodecMaker.make
  implicit val taskCodec: JsonValueCodec[Task] = JsonCodecMaker.make
  implicit val workflowCodec: JsonValueCodec[Workflow] = JsonCodecMaker.make
  implicit val taskResultMapCodec: JsonValueCodec[Map[UUID, TaskResult]] =
    JsonCodecMaker.make

  private val schemaManager = SchemaManager()
  private val connectionUrl = s"jdbc:sqlite:$dbPath"

  // Initialize database schema
  locally {
    withConnection { conn =>
      schemaManager.createTables(conn)
    }.recover { case e: Exception =>
      println(s"Failed to initialize database schema: ${e.getMessage}")
      throw e
    }
  }

  /** Executes a database operation with a connection.
    */
  private def withConnection[T](f: Connection => Future[T]): Future[T] = {
    val conn = DriverManager.getConnection(connectionUrl)
    f(conn).andThen { case _ =>
      conn.close()
    }
  }

  // Workflow operations

  override def saveWorkflow(workflow: Workflow): Future[Workflow] =
    withConnection { conn =>
      Future {
        val sql = """
        INSERT INTO workflows (id, name, status, created_at, updated_at, tasks_json, variables_json)
        VALUES (?, ?, ?, ?, ?, ?, ?)
      """
        val stmt = conn.prepareStatement(sql)
        try {
          stmt.setString(1, workflow.id.toString)
          stmt.setString(2, workflow.name)
          stmt.setString(3, workflow.status.toString)
          stmt.setLong(4, workflow.createdAt)
          stmt.setLong(5, workflow.updatedAt)
          stmt.setString(6, writeToString(workflow.tasks))
          stmt.setString(7, writeToString(workflow.variables))
          stmt.executeUpdate()
          workflow
        } finally {
          stmt.close()
        }
      }
    }

  override def getWorkflow(id: ID): Future[Option[Workflow]] = withConnection {
    conn =>
      Future {
        val sql = "SELECT * FROM workflows WHERE id = ?"
        val stmt = conn.prepareStatement(sql)
        try {
          stmt.setString(1, id.toString)
          val rs = stmt.executeQuery()
          if (rs.next()) {
            Some(extractWorkflow(rs))
          } else {
            None
          }
        } finally {
          stmt.close()
        }
      }
  }

  override def listWorkflows(): Future[List[Workflow]] = withConnection {
    conn =>
      Future {
        val sql = "SELECT * FROM workflows ORDER BY created_at DESC"
        val stmt = conn.createStatement()
        try {
          val rs = stmt.executeQuery(sql)
          val workflows = ListBuffer[Workflow]()
          while (rs.next()) {
            workflows += extractWorkflow(rs)
          }
          workflows.toList
        } finally {
          stmt.close()
        }
      }
  }

  override def updateWorkflow(workflow: Workflow): Future[Workflow] =
    withConnection { conn =>
      Future {
        val sql = """
        UPDATE workflows
        SET name = ?, status = ?, updated_at = ?, tasks_json = ?, variables_json = ?
        WHERE id = ?
      """
        val stmt = conn.prepareStatement(sql)
        try {
          stmt.setString(1, workflow.name)
          stmt.setString(2, workflow.status.toString)
          stmt.setLong(3, System.currentTimeMillis())
          stmt.setString(4, writeToString(workflow.tasks))
          stmt.setString(5, writeToString(workflow.variables))
          stmt.setString(6, workflow.id.toString)
          stmt.executeUpdate()
          workflow.copy(updatedAt = System.currentTimeMillis())
        } finally {
          stmt.close()
        }
      }
    }

  override def deleteWorkflow(id: ID): Future[Boolean] = withConnection {
    conn =>
      Future {
        val sql = "DELETE FROM workflows WHERE id = ?"
        val stmt = conn.prepareStatement(sql)
        try {
          stmt.setString(1, id.toString)
          stmt.executeUpdate() > 0
        } finally {
          stmt.close()
        }
      }
  }

  // Task operations

  override def saveTask(task: Task): Future[Task] = withConnection { conn =>
    Future {
      val sql = """
        INSERT INTO tasks (id, workflow_id, name, status, created_at, updated_at, task_type,
                          parameters_json, retries, max_retries, dependencies_json)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      """
      val stmt = conn.prepareStatement(sql)
      try {
        stmt.setString(1, task.id.toString)
        stmt.setString(
          2,
          "unknown"
        ) // We'll need to track workflow_id separately
        stmt.setString(3, task.name)
        stmt.setString(4, task.status.toString)
        stmt.setLong(5, task.createdAt)
        stmt.setLong(6, task.updatedAt)
        stmt.setString(7, task.taskType)
        stmt.setString(8, writeToString(task.parameters))
        stmt.setInt(9, task.retries)
        stmt.setInt(10, task.maxRetries)
        stmt.setString(11, writeToString(task.dependencies))
        stmt.executeUpdate()
        task
      } finally {
        stmt.close()
      }
    }
  }

  override def getTask(id: ID): Future[Option[Task]] = withConnection { conn =>
    Future {
      val sql = "SELECT * FROM tasks WHERE id = ?"
      val stmt = conn.prepareStatement(sql)
      try {
        stmt.setString(1, id.toString)
        val rs = stmt.executeQuery()
        if (rs.next()) {
          Some(extractTask(rs))
        } else {
          None
        }
      } finally {
        stmt.close()
      }
    }
  }

  override def updateTask(task: Task): Future[Task] = withConnection { conn =>
    Future {
      val sql = """
        UPDATE tasks
        SET name = ?, status = ?, updated_at = ?, task_type = ?, parameters_json = ?,
            retries = ?, max_retries = ?, dependencies_json = ?
        WHERE id = ?
      """
      val stmt = conn.prepareStatement(sql)
      try {
        stmt.setString(1, task.name)
        stmt.setString(2, task.status.toString)
        stmt.setLong(3, System.currentTimeMillis())
        stmt.setString(4, task.taskType)
        stmt.setString(5, writeToString(task.parameters))
        stmt.setInt(6, task.retries)
        stmt.setInt(7, task.maxRetries)
        stmt.setString(8, writeToString(task.dependencies))
        stmt.setString(9, task.id.toString)
        stmt.executeUpdate()
        task.copy(updatedAt = System.currentTimeMillis())
      } finally {
        stmt.close()
      }
    }
  }

  override def deleteTask(id: ID): Future[Boolean] = withConnection { conn =>
    Future {
      val sql = "DELETE FROM tasks WHERE id = ?"
      val stmt = conn.prepareStatement(sql)
      try {
        stmt.setString(1, id.toString)
        stmt.executeUpdate() > 0
      } finally {
        stmt.close()
      }
    }
  }

  // Workflow result operations

  override def saveWorkflowResult(
      result: WorkflowResult
  ): Future[WorkflowResult] = withConnection { conn =>
    Future {
      val sql = """
        INSERT OR REPLACE INTO workflow_results (workflow_id, task_results_json, error, created_at)
        VALUES (?, ?, ?, ?)
      """
      val stmt = conn.prepareStatement(sql)
      try {
        stmt.setString(1, result.workflowId.toString)
        stmt.setString(2, writeToString(result.taskResults))
        stmt.setString(3, result.error.orNull)
        stmt.setLong(4, System.currentTimeMillis())
        stmt.executeUpdate()
        result
      } finally {
        stmt.close()
      }
    }
  }

  override def getWorkflowResult(
      workflowId: ID
  ): Future[Option[WorkflowResult]] = withConnection { conn =>
    Future {
      val sql = "SELECT * FROM workflow_results WHERE workflow_id = ?"
      val stmt = conn.prepareStatement(sql)
      try {
        stmt.setString(1, workflowId.toString)
        val rs = stmt.executeQuery()
        if (rs.next()) {
          val taskResultsJson = rs.getString("task_results_json")
          val error = Option(rs.getString("error"))
          val taskResults =
            readFromString[Map[UUID, TaskResult]](taskResultsJson)
          Some(WorkflowResult(workflowId, taskResults, error))
        } else {
          None
        }
      } finally {
        stmt.close()
      }
    }
  }

  // Task result operations

  override def saveTaskResult(result: TaskResult): Future[TaskResult] =
    withConnection { conn =>
      Future {
        val sql = """
        INSERT OR REPLACE INTO task_results (task_id, output, error, created_at)
        VALUES (?, ?, ?, ?)
      """
        val stmt = conn.prepareStatement(sql)
        try {
          stmt.setString(1, result.taskId.toString)
          stmt.setString(2, result.output)
          stmt.setString(3, result.error.orNull)
          stmt.setLong(4, System.currentTimeMillis())
          stmt.executeUpdate()
          result
        } finally {
          stmt.close()
        }
      }
    }

  override def getTaskResult(taskId: ID): Future[Option[TaskResult]] =
    withConnection { conn =>
      Future {
        val sql = "SELECT * FROM task_results WHERE task_id = ?"
        val stmt = conn.prepareStatement(sql)
        try {
          stmt.setString(1, taskId.toString)
          val rs = stmt.executeQuery()
          if (rs.next()) {
            val output = rs.getString("output")
            val error = Option(rs.getString("error"))
            Some(TaskResult(taskId, output, error))
          } else {
            None
          }
        } finally {
          stmt.close()
        }
      }
    }

  // Transition operations

  override def saveTransition(transition: Transition): Future[Transition] =
    withConnection { conn =>
      Future {
        val sql = """
        INSERT INTO transitions (entity_id, from_status, to_status, timestamp)
        VALUES (?, ?, ?, ?)
      """
        val stmt = conn.prepareStatement(sql)
        try {
          stmt.setString(1, transition.entityId.toString)
          stmt.setString(2, transition.fromStatus.toString)
          stmt.setString(3, transition.toStatus.toString)
          stmt.setLong(4, transition.timestamp)
          stmt.executeUpdate()
          transition
        } finally {
          stmt.close()
        }
      }
    }

  override def getTransitions(entityId: ID): Future[List[Transition]] =
    withConnection { conn =>
      Future {
        val sql =
          "SELECT * FROM transitions WHERE entity_id = ? ORDER BY timestamp ASC"
        val stmt = conn.prepareStatement(sql)
        try {
          stmt.setString(1, entityId.toString)
          val rs = stmt.executeQuery()
          val transitions = ListBuffer[Transition]()
          while (rs.next()) {
            val fromStatus = parseStatus(rs.getString("from_status"))
            val toStatus = parseStatus(rs.getString("to_status"))
            val timestamp = rs.getLong("timestamp")
            transitions += Transition(entityId, fromStatus, toStatus, timestamp)
          }
          transitions.toList
        } finally {
          stmt.close()
        }
      }
    }

  // Helper methods

  private def extractWorkflow(rs: ResultSet): Workflow = {
    val id = UUID.fromString(rs.getString("id"))
    val name = rs.getString("name")
    val status = parseStatus(rs.getString("status"))
    val createdAt = rs.getLong("created_at")
    val updatedAt = rs.getLong("updated_at")
    val tasksJson = rs.getString("tasks_json")
    val variablesJson = rs.getString("variables_json")
    val tasks = readFromString[List[Task]](tasksJson)
    val variables = readFromString[Map[String, String]](variablesJson)
    Workflow(id, name, status, createdAt, updatedAt, tasks, variables)
  }

  private def extractTask(rs: ResultSet): Task = {
    val id = UUID.fromString(rs.getString("id"))
    val name = rs.getString("name")
    val status = parseStatus(rs.getString("status"))
    val createdAt = rs.getLong("created_at")
    val updatedAt = rs.getLong("updated_at")
    val taskType = rs.getString("task_type")
    val parametersJson = rs.getString("parameters_json")
    val retries = rs.getInt("retries")
    val maxRetries = rs.getInt("max_retries")
    val dependenciesJson = rs.getString("dependencies_json")
    val parameters = readFromString[Map[String, String]](parametersJson)
    val dependencies = readFromString[Set[UUID]](dependenciesJson)
    Task(
      id,
      name,
      status,
      createdAt,
      updatedAt,
      taskType,
      parameters,
      retries,
      maxRetries,
      dependencies
    )
  }

  private def parseStatus(s: String): Status = s match {
    case "Pending"   => Pending
    case "Running"   => Running
    case "Completed" => Completed
    case "Failed"    => Failed
    case other => throw new IllegalArgumentException(s"Unknown status: $other")
  }
}

object SQLitePersistence {
  def apply(dbPath: String)(implicit ec: ExecutionContext): SQLitePersistence =
    new SQLitePersistence(dbPath)
}
