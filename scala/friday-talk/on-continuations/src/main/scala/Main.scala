import scala.annotation.tailrec
import scala.util.control.TailCalls.{TailRec, done, tailcall}

def fact0(n: Long): BigDecimal =
  if (n == 0) {
    1
  } else {
    n * fact0(n - 1)
  }

@tailrec
def fact1(n: BigDecimal, acc: BigDecimal = 1): BigDecimal =
  if (n == 0) {
    acc
  } else {
    fact1(n - 1, n * acc)
  }

def factorial(n: Long): TailRec[BigDecimal] =
  if (n == 0) {
    done(1)
  } else {
    tailcall(factorial(n - 1))
      .map(result => result * n)
  }

// TODO: use function compostion to accumulate
def cpsFact(n: BigDecimal, k: BigDecimal => BigDecimal): BigDecimal = {
  if (n == 0L) k(1) // Pass the continuation for the base case
  else
    cpsFact(
      n - 1,
      (result: BigDecimal) => k(n * result)
    )
}

@main def hello(): Unit =
  val n = 100_000_000L
  val n2 = 10
  println(Console.YELLOW + "-" * 120 + Console.RESET)
  println(s"Factorial of $n is ${factorial(1_000_000).result}")
  println(s"Factorial of $n is ${fact1(1_000_000)}")
  println(s"CPS fact: ${cpsFact(100000L, identity)}")
  println(Console.YELLOW + "-" * 120 + Console.RESET)
