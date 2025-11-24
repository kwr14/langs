package uk.sky.kurate.persistence

import java.sql.Connection
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Try, Success, Failure}

/**
 * Manages database schema creation and migration for SQLite persistence.
 */
class SchemaManager(implicit ec: ExecutionContext) {

  /**
   * Creates all required tables if they don't exist.
   * This operation is idempotent.
   */
  def createTables(conn: Connection): Future[Unit] = Future {
    val stmt = conn.createStatement()
    try {
      // Enable WAL mode for better concurrent access
      stmt.execute("PRAGMA journal_mode=WAL")
      stmt.execute("PRAGMA synchronous=NORMAL")

      // Workflows table
      stmt.execute("""
        CREATE TABLE IF NOT EXISTS workflows (
          id TEXT PRIMARY KEY,
          name TEXT NOT NULL,
          status TEXT NOT NULL,
          created_at INTEGER NOT NULL,
          updated_at INTEGER NOT NULL,
          tasks_json TEXT NOT NULL,
          variables_json TEXT NOT NULL
        )
      """)

      // Tasks table
      stmt.execute("""
        CREATE TABLE IF NOT EXISTS tasks (
          id TEXT PRIMARY KEY,
          workflow_id TEXT NOT NULL,
          name TEXT NOT NULL,
          status TEXT NOT NULL,
          created_at INTEGER NOT NULL,
          updated_at INTEGER NOT NULL,
          task_type TEXT NOT NULL,
          parameters_json TEXT NOT NULL,
          retries INTEGER NOT NULL DEFAULT 0,
          max_retries INTEGER NOT NULL DEFAULT 3,
          dependencies_json TEXT NOT NULL,
          FOREIGN KEY (workflow_id) REFERENCES workflows(id) ON DELETE CASCADE
        )
      """)

      // Workflow results table
      stmt.execute("""
        CREATE TABLE IF NOT EXISTS workflow_results (
          workflow_id TEXT PRIMARY KEY,
          task_results_json TEXT NOT NULL,
          error TEXT,
          created_at INTEGER NOT NULL,
          FOREIGN KEY (workflow_id) REFERENCES workflows(id) ON DELETE CASCADE
        )
      """)

      // Task results table
      stmt.execute("""
        CREATE TABLE IF NOT EXISTS task_results (
          task_id TEXT PRIMARY KEY,
          output TEXT NOT NULL,
          error TEXT,
          created_at INTEGER NOT NULL,
          FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
        )
      """)

      // Transitions table (audit trail)
      stmt.execute("""
        CREATE TABLE IF NOT EXISTS transitions (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          entity_id TEXT NOT NULL,
          from_status TEXT NOT NULL,
          to_status TEXT NOT NULL,
          timestamp INTEGER NOT NULL
        )
      """)

      // Create indexes for performance
      stmt.execute("CREATE INDEX IF NOT EXISTS idx_tasks_workflow_id ON tasks(workflow_id)")
      stmt.execute("CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status)")
      stmt.execute("CREATE INDEX IF NOT EXISTS idx_workflows_status ON workflows(status)")
      stmt.execute("CREATE INDEX IF NOT EXISTS idx_transitions_entity_id ON transitions(entity_id)")
      stmt.execute("CREATE INDEX IF NOT EXISTS idx_transitions_timestamp ON transitions(timestamp)")

    } finally {
      stmt.close()
    }
  }

  /**
   * Drops all tables. Used for testing.
   */
  def dropTables(conn: Connection): Future[Unit] = Future {
    val stmt = conn.createStatement()
    try {
      stmt.execute("DROP TABLE IF EXISTS transitions")
      stmt.execute("DROP TABLE IF EXISTS task_results")
      stmt.execute("DROP TABLE IF EXISTS workflow_results")
      stmt.execute("DROP TABLE IF EXISTS tasks")
      stmt.execute("DROP TABLE IF EXISTS workflows")
    } finally {
      stmt.close()
    }
  }

  /**
   * Gets the current schema version.
   * Returns 1 for now (no migrations yet).
   */
  def getSchemaVersion(conn: Connection): Future[Int] = Future {
    // For now, we just return version 1
    // In the future, we could store this in a schema_version table
    1
  }
}

object SchemaManager {
  def apply()(implicit ec: ExecutionContext): SchemaManager = new SchemaManager()
}

