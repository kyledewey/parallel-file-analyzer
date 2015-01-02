package parfile.backend

object ParallelTask {
  // returns the thread that is running it
  def runInThread(toRun: => Unit): Thread = {
    val retval =
      new Thread(
        new Runnable {
          def run() {
            toRun
          }
        })
    retval.start()
    retval
  }
}

// Takes a series of Producers which have not yet been started,
// along with the stream that they are to attach to.  This
// is used to determine when to shutdown the stream.
class StreamManager[T](
  producerMakers: Seq[MultiStreamProducerInterface[T] => Producer[T]],
  consumerMakers: Seq[MultiStreamConsumerInterface[T] => Consumer[T]],
  stream: MultiStream[T]) {
  import java.util.concurrent.atomic.AtomicInteger
  import ParallelTask.runInThread

  private val numRemaining = new AtomicInteger(producerMakers.size)
  private val watcher = new Object

  protected def makeSomethingFromStream[A](fs: Seq[MultiStream[T] => A]): Seq[A] =
    fs.map(f => f(stream))

  protected def makeConsumers() {
    makeSomethingFromStream(consumerMakers).foreach(consumer =>
      runInThread {
        consumer.start()
      })
  }

  protected def makeProducers() {
    makeSomethingFromStream(producerMakers).foreach(producer =>
      runInThread {
        producer.start()
        watcher.synchronized {
          numRemaining.decrementAndGet()
          watcher.notify()
        }
      })
  }

  // Runs until all producers and consumers have finished.
  def runManager() {
    makeProducers()
    makeConsumers()

    watcher.synchronized {
      while (numRemaining.get != 0) {
        watcher.wait()
      }
    }

    stream.shutdown()
  }
}

