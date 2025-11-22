package cass4io.domain.movie.persistence

import cats.effect.IO
import cass4io.domain.movie.model.RecordMetadata
import com.datastax.oss.driver.api.core.cql.ResultSet
import _root_.cass4io.domain.movie.model.ColumnInfo
import com.datastax.oss.driver.api.core.cql.BoundStatement
import java.time.Instant
import java.util.concurrent.CompletionStage
import com.datastax.oss.driver.api.core.cql.AsyncResultSet
import com.datastax.oss.driver.api.core.cql.Row
import scala.concurrent.impl.Promise
import scala.concurrent.Promise
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletionStage
import scala.concurrent.{ExecutionContextExecutor, Future, Promise}
import scala.jdk.CollectionConverters._
import scala.jdk.FutureConverters.CompletionStageOps
import _root_.cass4io.domain.movie.model.FieldInfo
import _root_.cass4io.domain.movie.model.Movie

trait Repository[T] {
  def insert(
      row: T
  )(implicit ctx: CommonCtx, meta: RecordMetadata): IO[List[Row]]

  def update(
      row: T
  )(implicit
      ctx: CommonCtx,
      meta: RecordMetadata
  ): IO[List[Row]]

  def findById(
      id: String
  )(implicit
      ctx: CommonCtx,
      meta: RecordMetadata
  ): IO[Option[Movie]]

  def setBoundValue(
      c: FieldInfo,
      bd: BoundStatement
  ): BoundStatement = {

    c.typ.toString match {
      case "String" =>
        bd.setString(c.name, c.value.asInstanceOf[String])
      case "Option[String]" =>
        bd.setString(
          c.name,
          c.value.asInstanceOf[Option[String]].orNull
        )
      case "java.time.Instant" | "Instant" =>
        bd.setInstant(c.name, c.value.asInstanceOf[Instant])
      case "Option[java.time.Instant]" | "Option[Instant]" =>
        bd.setInstant(
          c.name,
          c.value.asInstanceOf[Option[Instant]].orNull
        )
    }
  }

  def fromJavaAsync(
      completionStage: => CompletionStage[AsyncResultSet]
  ): IO[List[Row]] = {
    val promise = Promise[List[Row]]()

    completionStage.handle[Any] { (resultSet, throwable) =>
      if (throwable != null) {
        promise.failure(throwable)
      } else {
        promise.success(resultSet.currentPage().asScala.toList)
      }
    }
    IO.fromFuture(IO(promise.future))
  }
}
