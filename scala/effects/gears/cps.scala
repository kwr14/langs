//> using scala "3.5.0"
//> using file "utils.scala"

import scala.annotation.tailrec
import Utils.timeIt
import scala.math.*

object FunWithRec extends App {

// @tailrec
  def fact(n: Int): BigDecimal = {
    if (n == 0) 1
    else n * fact(n - 1)
  }

  @tailrec
  def cpsFact(n: Int, k: BigDecimal => BigDecimal): BigDecimal = {
    if (n == 0) k(1)
    else {
      cpsFact(n - 1, x => k(n * x))
    }
  }

  @tailrec
  def cpsFact2(n: Int, xs: List[BigDecimal]): BigDecimal = {
    if (n == 0) xs.product
    else {
      cpsFact2(n - 1, xs :+ n)
    }
  }

  def recFact(n: Int): BigDecimal = {

    @tailrec
    def cpsFact3(n: Int, acc: BigDecimal): BigDecimal = {
      if (n == 0) acc
      else {
        cpsFact3(n - 1, n * acc)
      }
    }
    cpsFact3(n, 1)
  }

  val n = 100000
  // println(timeIt(recFact(n)))
  // println(cpsFact3(n, 1.0))
  // println(timeIt(cpsFact2(n, Nil)))

  cpsFact(6, pprint)

  def pprint(n: BigDecimal): BigDecimal = {
    println(s"Result: ${n.toString()}")
    n
  }
// fact(n)
}
