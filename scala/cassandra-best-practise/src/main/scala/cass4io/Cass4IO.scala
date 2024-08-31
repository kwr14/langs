package cass4io

import cass4io.CassApp.CommonCtx
import cass4io.Utils.{ColumnInfo, listCaseClassFields}
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql._
import com.datastax.oss.driver.api.querybuilder.QueryBuilder._
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert
import java.time.Instant
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._
import com.datastax.oss.driver.api.core.ConsistencyLevel
import com.datastax.oss.driver.api.querybuilder.update.Assignment
import com.datastax.oss.driver.api.querybuilder.update.UpdateWithAssignments

/** The Cass4IO trait defines the contract for interacting with a Cassandra
  * database.
  */
trait Cass4IO {

  /** Safely inserts a row of data into the specified table in the Cassandra
    * database.
    *
    * @param table
    *   The name of the table to insert the row into.
    * @param row
    *   The row of data to be inserted.
    * @param commonCtx
    *   The common context required for the insertion operation.
    * @tparam T
    *   The type of the row, which must have TypeTag and ClassTag implicitly
    *   available.
    * @return
    *   The ResultSet representing the result of the insertion operation.
    */
  def safeInsert[T: TypeTag: ClassTag](
      table: String,
      row: T
  )(implicit commonCtx: CommonCtx): ResultSet

  /** Safely updates a row of data in the specified table in the Cassandra
    * database.
    *
    * @param table
    *   The name of the table to update the row in.
    * @param row
    *   The row of data to be updated.
    * @param primaryKey
    *   The primary key of the row to be updated.
    * @param commonCtx
    *   The common context required for the update operation.
    * @tparam T
    *   The type of the row, which must have TypeTag and ClassTag implicitly
    *   available.
    * @return
    *   The ResultSet representing the result of the update operation.
    */
  def safeUpdate[T: TypeTag: ClassTag](
      table: String,
      row: T,
      primaryKey: String
  )(implicit commonCtx: CommonCtx): ResultSet
}

case class LiveCass4IO(session: CqlSession) extends Cass4IO {
  override def safeInsert[T: TypeTag: ClassTag](
      table: String,
      row: T
  )(implicit commonCtx: CommonCtx): ResultSet = {

    def simpleStatementGen(
        columnNames: List[String],
        table: String
    ): SimpleStatement = {
      val firstColumnName = columnNames.head

      val regularInsertBuilder: RegularInsert =
        insertInto(commonCtx.keyspace, table)
          .value(firstColumnName, bindMarker(firstColumnName))

      columnNames.tail
        .foldRight[RegularInsert](regularInsertBuilder) {
          case (next, regularInsertAcc) =>
            regularInsertAcc.value(next, bindMarker(next))
        }
        .build()
    }

    def boundStatementGen(
        simpleStatement: SimpleStatement,
        columns: List[ColumnInfo]
    ): BoundStatement = {
      val boundStatementBuilder: BoundStatement =
        session.prepare(simpleStatement).bind()

      def setBoundValue(
          c: ColumnInfo,
          bd: BoundStatement
      ): BoundStatement = {
        c.columnType.toString match {
          case "String" =>
            bd.setString(c.columnName, c.columnValue.asInstanceOf[String])
          case "Option[String]" =>
            bd.setString(
              c.columnName,
              c.columnValue.asInstanceOf[Option[String]].orNull
            )
          case "java.time.Instant" | "Instant" =>
            bd.setInstant(c.columnName, c.columnValue.asInstanceOf[Instant])
          case "Option[java.time.Instant]" | "Option[Instant]" =>
            bd.setInstant(
              c.columnName,
              c.columnValue.asInstanceOf[Option[Instant]].orNull
            )
        }
      }

      val boundStatement =
        columns.foldRight[BoundStatement](boundStatementBuilder) {
          case (next, boundStatementAcc) =>
            setBoundValue(next, boundStatementAcc)
        }

      // unset the null fields
      columns
        .foldRight(boundStatement) {
          case (ColumnInfo(columnName, _, None), acc) => acc.unset(columnName)
          case (_, acc)                               => acc
        }
        .setConsistencyLevel(commonCtx.consistencyLevel)
    }

    lazy val columns: List[ColumnInfo] = listCaseClassFields(row)

    // 1): prepare statement
    lazy val simpleStatement: SimpleStatement =
      simpleStatementGen(columnNames = columns.map(_.columnName), table = table)

    // 2): bound values
    lazy val boundStatement = boundStatementGen(simpleStatement, columns)

    // 3): execute the query
    session.execute(boundStatement)
  }

  override def safeUpdate[T: TypeTag: ClassTag](
      table: String,
      row: T,
      primaryKey: String
  )(implicit commonCtx: CommonCtx): ResultSet = {

    // Get the columns from the row
    lazy val columns: List[ColumnInfo] = listCaseClassFields(row)

    // Filter out columns with None values
    val nonNullColumns =
      columns
        .filter(_.columnValue != None)

    println(s"Filter out columns with $nonNullColumns columns")

    if (nonNullColumns.isEmpty) {
      throw new IllegalArgumentException("No non-null values to update.")
    }

    // Generate the update statement
    def updateStatementGen(
        columnNames: List[String],
        table: String,
        primaryKey: String
    ): SimpleStatement = {

      val onGoingUpdateBuilder: UpdateWithAssignments =
        update(commonCtx.keyspace, table)
          .setColumn(columnNames.head, bindMarker(columnNames.head))

      val updateBuilder: UpdateWithAssignments =
        columnNames.tail.foldRight(onGoingUpdateBuilder) {
          case (nextColumn, ub) =>
            ub.setColumn(nextColumn, bindMarker(nextColumn))
        }

      val updateBuilderWithWhere =
        updateBuilder.whereColumn("isbn").isEqualTo(bindMarker())

      updateBuilderWithWhere.build()
    }

    def boundStatementGen(
        simpleStatement: SimpleStatement,
        columns: List[ColumnInfo]
    ): BoundStatement = {
      val boundStatementBuilder: BoundStatement =
        session.prepare(simpleStatement).bind()

      columns.foldRight(boundStatementBuilder) { case (c, bd) =>
        c.columnType.toString match {
          case "String" =>
            bd.setString(c.columnName, c.columnValue.asInstanceOf[String])
          case "Option[String]" =>
            bd.setString(
              c.columnName,
              c.columnValue.asInstanceOf[Option[String]].orNull
            )
          case "java.time.Instant" | "Instant" =>
            bd.setInstant(c.columnName, c.columnValue.asInstanceOf[Instant])
          case "Option[java.time.Instant]" | "Option[Instant]" =>
            bd.setInstant(
              c.columnName,
              c.columnValue.asInstanceOf[Option[Instant]].orNull
            )
        }
      }
    }

    // 1): prepare statement
    lazy val simpleStatement: SimpleStatement =
      updateStatementGen(
        columnNames =
          nonNullColumns.filter(c => c.columnName != "isbn").map(_.columnName),
        table = table,
        primaryKey = primaryKey
      )

    // 2): bound values
    lazy val boundStatement = boundStatementGen(simpleStatement, nonNullColumns)

    // 3): execute the query
    session.execute(boundStatement)
  }
}
