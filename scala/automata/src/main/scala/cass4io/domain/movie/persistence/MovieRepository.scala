package cass4io.domain.movie.persistence

import cass4io.domain.movie.model.Movie
import cats.effect.IO
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.querybuilder.QueryBuilder._
import com.datastax.oss.driver.api.querybuilder.insert.Insert
import com.datastax.oss.driver.api.querybuilder.update.Update
import com.datastax.oss.driver.api.querybuilder.delete.Delete
import com.datastax.oss.driver.api.querybuilder.select.Select
import com.datastax.oss.driver.api.core.cql.SimpleStatement
import scala.jdk.CollectionConverters._
import cass4io.domain.movie.model.RecordMetadata
import com.datastax.oss.driver.api.core.cql.ResultSet
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert
import cass4io.domain.movie.model.ColumnInfo
import com.datastax.oss.driver.api.core.cql.BoundStatement
import java.time.Instant
import _root_.cass4io.domain.movie.model.FieldInfo
import com.datastax.oss.driver.api.core.cql.Row
import com.datastax.oss.driver.api.querybuilder.QueryBuilder
import com.datastax.oss.driver.api.querybuilder.update.UpdateWithAssignments
import com.datastax.oss.driver.api.core.cql.AsyncResultSet
import java.util.concurrent.CompletionStage
import com.datastax.oss.driver.api.core.cql.ColumnDefinition
import scala.util.Try

class MovieRepository(session: CqlSession) extends Repository[Movie] {
  override def insert(
      row: Movie
  )(implicit ctx: CommonCtx, meta: RecordMetadata): IO[List[Row]] = {

    def simpleStatementGen(
        columnNames: List[String],
        table: String
    )(implicit ctx: CommonCtx, meta: RecordMetadata): SimpleStatement = {

      val firstColumnName :: remainingNames = columnNames

      val regularInsertBuilder: RegularInsert =
        insertInto(ctx.keyspace, meta.tableName)
          .value(firstColumnName, bindMarker(firstColumnName))

      remainingNames
        .foldRight[RegularInsert](regularInsertBuilder) {
          case (next, regularInsertAcc) =>
            regularInsertAcc.value(next, bindMarker(next))
        }
        .build()
    }

    def boundStatementGen(
        simpleStatement: SimpleStatement,
        columns: List[FieldInfo]
    ): BoundStatement = {
      val boundStatementBuilder: BoundStatement =
        session.prepare(simpleStatement).bind()

      val boundStatement =
        columns.foldRight[BoundStatement](boundStatementBuilder) {
          case (next, boundStatementAcc) =>
            setBoundValue(next, boundStatementAcc)
        }

      // unset the null fields
      columns
        .foldRight(boundStatement) {
          case (FieldInfo(columnName, _, None), acc) => acc.unset(columnName)
          case (_, acc)                              => acc
        }
        .setConsistencyLevel(ctx.consistencyLevel)
    }

    // 1): prepare statement
    lazy val simpleStatement: SimpleStatement =
      simpleStatementGen(
        columnNames = meta.fieldsInfo.map(_.name),
        table = meta.tableName
      )

    // 2): bound values
    lazy val boundStatement =
      boundStatementGen(simpleStatement, meta.fieldsInfo)

    // 3): execute the query
    fromJavaAsync(session.executeAsync(boundStatement))
  }

  override def update(
      row: Movie
  )(implicit
      ctx: CommonCtx,
      meta: RecordMetadata
  ): IO[List[Row]] = {

    // Get the columns from the row
    lazy val columns: List[FieldInfo] = meta.fieldsInfo

    // Filter out columns with None values
    val nonNullColumns =
      columns
        .filter(_.value != None)

    if (nonNullColumns.isEmpty) {
      throw new IllegalArgumentException("No non-null values to update.")
    }

    // Generate the update statement
    def updateStatementGen(
        columnNames: List[String]
    )(implicit
        ctx: CommonCtx,
        meta: RecordMetadata
    ): SimpleStatement = {

      val firstColumnName :: remainingNames = columnNames

      val onGoingUpdateBuilder: UpdateWithAssignments =
        QueryBuilder
          .update(ctx.keyspace, meta.tableName)
          .setColumn(firstColumnName, bindMarker(firstColumnName))

      val updateBuilder: UpdateWithAssignments =
        remainingNames.foldRight(onGoingUpdateBuilder) {
          case (nextColumn, ub) =>
            ub.setColumn(nextColumn, bindMarker(nextColumn))
        }

      val updateBuilderWithWhere =
        updateBuilder.whereColumn(meta.primaryKey).isEqualTo(bindMarker())

      updateBuilderWithWhere.build()
    }

    def boundStatementGen(
        simpleStatement: SimpleStatement,
        columns: List[FieldInfo]
    ): BoundStatement = {
      val boundStatementBuilder: BoundStatement =
        session.prepare(simpleStatement).bind()

      columns.foldRight(boundStatementBuilder) { case (c, bd) =>
        setBoundValue(c, bd)
      }
    }

    // 1): prepare statement
    lazy val simpleStatement: SimpleStatement =
      updateStatementGen(
        columnNames =
          nonNullColumns.filter(c => c.name != meta.primaryKey).map(_.name)
      )

    // 2): bound values
    lazy val boundStatement = boundStatementGen(simpleStatement, nonNullColumns)

    // 3): execute the query
    fromJavaAsync(session.executeAsync(boundStatement))
  }

  override def findById(
      isbn: String
  )(implicit
      ctx: CommonCtx,
      meta: RecordMetadata
  ): IO[Option[Movie]] = {

    // Generate the select statement
    val selectStatement: SimpleStatement =
      QueryBuilder
        .selectFrom(ctx.keyspace, meta.tableName)
        .all()
        .whereColumn(meta.primaryKey)
        .isEqualTo(bindMarker(isbn))
        .build()

    // Prepare the statement
    val statement = session
      .prepare(selectStatement)
      .bind()
      .setString(meta.primaryKey, isbn)
      .setConsistencyLevel(ctx.consistencyLevel)

    // Execute the query
    val resultSet: CompletionStage[AsyncResultSet] =
      session.executeAsync(statement)

    fromJavaAsync(resultSet).map { resultSet =>
      resultSet.headOption.map(r => Movie.parse(r).get)
    }
  }

}
