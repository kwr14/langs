object Utils {
  def timeIt[R](block: => R): R = {
    val start = System.nanoTime()
    val result = block
    val end = System.nanoTime()
    println(s"Time taken: ${(end - start) / 1e6} ms")
    result
  }
}
