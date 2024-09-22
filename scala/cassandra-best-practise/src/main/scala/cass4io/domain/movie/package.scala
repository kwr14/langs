package cass4io.domain

import com.datastax.oss.protocol.internal.ProtocolConstants.ConsistencyLevel
import scala.concurrent.ExecutionContext
import java.time.Instant

package object movie {

  case class CommonCtx(
      keyspace: String,
      consistencyLevel: ConsistencyLevel,
      ec: Option[ExecutionContext] = None
  )
}
