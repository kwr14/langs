import scala.annotation.tailrec
import scala.util.control.TailCalls.TailRec

def f0(x: Int): TailRec[Double] = {
  if (x == 0) 0
  else f0(x - 1).add(Double(x))
}

f0(10000)

def factorial(x: Long): BigDecimal = x match {
  case 0 => 1
  case 1 => 1
  case _ => x * factorial(x - 1)
}

factorial(10000)

// transform it into CPS
// @tailrec
def fact0(x: Long, k: BigDecimal => BigDecimal): BigDecimal = {
  if (x == 0) k(1)
  else fact0(x - 1, y => k(x * y))
}

def identity(x: BigDecimal): BigDecimal = x

fact0(10000, identity)

def fact1(x: Long, acc: List[Long]): List[Long] = {
  if (x == 0) acc
  else fact1(x - 1, x :: acc)
}

fact1(100, List())

// 5!  = K(5 * fact(4)) = 5 * fact(4)

// * is associtive & how is it helping us?
// * what happens if it is not?
// todo: maybe

def fact2(x: Long): Long = {
  // tail recursion
  @tailrec
  def loop(x: Long, acc: Long): Long =
    if (x == 0) acc
    else loop(x - 1, x * acc)

  loop(x, 1)
}

fact2(100)

// inversion of control

// Continuations are used to transform into stack safe recursive functions
// excptions
// gnerators
// threads
// control flows
// direct styles
// algeberic effects & handlres
// kyo freez & continue
