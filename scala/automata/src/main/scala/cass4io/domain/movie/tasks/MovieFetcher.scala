package cass4io.domain.movie.tasks

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.ResultSet
import cass4io.domain.movie.model.Movie

class MovieFetcher(session: CqlSession) {
  def fetchMovies: ResultSet = {
    val selectStmt = session.prepare("SELECT * FROM cassandra_ref.movies")
    session.execute(selectStmt.bind())
  }
}
