import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.Future

// Create a task as a lambda function
val myTask: Runnable = () => {
  println("myTask")
  Thread.sleep(1000)
  println("myTask done")
}

// Create a thread pool with 2 threads
val threadPool: ExecutorService = Executors.newFixedThreadPool(2)

// Submit the task to the thread pool
val result: Int = threadPool
  .submit(myTask)
  .match
    case x: Future[_] => {
      1
    }
    case _ => {
      0
    }

result
// Shutdown the thread pool
// threadPool.shutdown()
