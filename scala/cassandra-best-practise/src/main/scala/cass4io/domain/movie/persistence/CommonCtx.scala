package cass4io.domain.movie.persistence
import com.datastax.oss.driver.api.core.ConsistencyLevel
import org.apache.http.protocol.ExecutionContext

case class CommonCtx(
    keyspace: String,
    consistencyLevel: ConsistencyLevel,
    ec: Option[ExecutionContext] = None
)
