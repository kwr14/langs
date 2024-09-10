//  Factoria
import scala.util.control.TailCalls._
import cats.instances.tailRec

// @tailRec
def factorial(n: Int): Int = {
  if (n == 0) 1
  else n * factorial(n - 1)
}

def tailFactorial(n: Int): TailRec[Int] = {
  if (n == 0) done(1)
  else tailcall(tailFactorial(n - 1).map(n * _))
}

// using generator

def factorialGen(n: Int): Int = {
  def factorialHelper(n: Int, acc: Int): Int = {
    if (n == 0) acc
    else factorialHelper(n - 1, n * acc)
  }
  factorialHelper(n, 1)
}

def factorialCPS(n: Int): TailRec[Int] = {
  def factorialHelper(n: Int, cont: Int => TailRec[Int]): TailRec[Int] = {
    if (n == 0) done(cont(1).result)
    else tailcall(factorialHelper(n - 1, x => done(cont(n * x).result)))
  }

  factorialHelper(n, x => done(x))
}

def cps_fact(n: Int, ret: Int => Unit): Unit = {
  tail_fact(n, 1, ret)
}

def tail_fact(n: Int, acc: Int, ret: Int => Unit): Unit = {
  if (n == 0) {
    ret(acc)
  } else {
    tail_fact(n - 1, acc * n, ret)
  }
}

cps_fact(5, (x) => println(x))

// Usage example
// factorial(30)
// tailFactorial(30).result
// factorialCPS(100).result

def fib(n: Int): TailRec[Int] =
  if (n < 2) done(n)
  else
    for {
      x <- tailcall(fib(n - 1))
      y <- tailcall(fib(n - 2))
    } yield x + y

fib(7).result

def isEven(xs: List[Int]): TailRec[Boolean] =
  if (xs.isEmpty) done(true) else tailcall(isOdd(xs.tail))

def isOdd(xs: List[Int]): TailRec[Boolean] =
  if (xs.isEmpty) done(false) else tailcall(isEven(xs.tail))

isEven((1 to 100000).toList).result
isEven((1 to 100000 + 1).toList).result

def factorialGenerator: LazyList[Int] = {
  lazy val factorials: LazyList[Int] =
    LazyList.iterate((1, 1)) { case (i, acc) => (i + 1, acc * i) }.map(_._2)
  factorials
}

// Create a lazy sequence for factorial values
val fGen = factorialGenerator

// Get factorial values using the lazy sequence
val firstFactorial = fGen(0)
val secondFactorial = fGen(1)
val thirdFactorial = fGen(5)