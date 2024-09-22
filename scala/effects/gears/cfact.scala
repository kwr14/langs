//> using scala "3.5.0"

// CPS
import scala.annotation.tailrec
import scala.util.control.TailCalls._

import scala.util.control.TailCalls.{TailRec, done, tailcall}

def simpleRecursiveFactorial(n: Int): BigDecimal = {
  if (n == 0) {
    Thread
      .currentThread()
      .getStackTrace
      .foreach(x => println(s"value of n=$n,stack=$x"))
    1.0
  } else {
    Thread
      .currentThread()
      .getStackTrace
      .foreach(x => println(s"value of n=$n,stack=$x"))
    n * simpleRecursiveFactorial(n - 1)
  }
}

@tailrec
def factCPS[T](n: Int, k: BigDecimal => T): T = {
  if (n == 0) {

    k(1.0)
  } else {

    factCPS(n - 1, ans => k(n * ans))
  }
}

def factCPS2(n: Int, k: BigDecimal): BigDecimal = {
  if (n == 0) return k
  else factCPS2(n - 1, n * k)
}

def traFact(n: Int): TailRec[BigDecimal] = {
  if (n == 0) done(1)
  else
    tailcall(traFact(n - 1)).map { ans =>
      n * ans
    }
}

def fact(n: Int) = factCPS2(n: Int, 1)

object CRecFunc extends App {
//   println(s"result: ${fact(10000)}")
//   println(s"Tra: ${traFact(10000).result}")
  factCPS(10, x => println(x))
//   println(s"simple rec: ${simpleRecursiveFactorial(10)}")
}
