import org.scalacheck.Prop.Exception

import scala.util.Try
//import cass4io.domain.movie.model.Movie
//import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, writeToString}
//import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
//
//import scala.util.Try
//
//implicit val codec: JsonValueCodec[Movie] = JsonCodecMaker.make
//
//val movie: Movie = Movie(isbn = "isbn", releasedate = java.time.Instant.now(), title = "title", partner = None, reprint = None)
//
//writeToString[Movie](movie)

Try(throw new IllegalAccessException).toOption