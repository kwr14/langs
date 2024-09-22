package cass4io.domain.movie.persistence
import com.datastax.oss.driver.api.core.ConsistencyLevel

import scala.concurrent.{ExecutionContext, Future}

case class CommonCtx(
    keyspace: String,
    consistencyLevel: ConsistencyLevel,
    ec: Option[ExecutionContext] = None
)
