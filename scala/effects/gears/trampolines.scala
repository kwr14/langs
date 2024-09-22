//> using scala "3.5.0"

import scala.util.control.TailCalls.{TailRec, done, tailcall}

def loop(n: Int, acc: Double = 1): TailRec[Double] = {
  if (n <= 0) {
    done(acc)
  } else {
    tailcall(loop(n - 1, acc)).map { ans =>
      n * ans
    }
  }
}

object TraApp extends App {
  val n = 6
  val result = loop(n).result
  println(s"The factorial of $n is $result")

}
