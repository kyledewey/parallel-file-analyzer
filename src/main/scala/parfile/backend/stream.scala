package parfile.backend

import java.util.concurrent._
  
case class StreamShutdownException() extends Exception

trait MultiStreamProducerInterface[T <: AnyRef] {
  // Puts the item in the stream, as long as the stream has not
  // been shutdown.  If it has been shutdown, it throws a
  // StreamShutdownException.
  def put(item: T): Unit
}

trait MultiStreamConsumerInterface[T <: AnyRef] {
  // Gets an item from the stream.  If the stream is empty and
  // not shutdown, it blocks.  If the stream is empty and
  // shutdown, it returns None
  def get(): Option[T]
}
  
trait MultiStream[T <: AnyRef] extends MultiStreamProducerInterface[T] with MultiStreamConsumerInterface[T] {
  // shuts down the stream
  def shutdown(): Unit
}

object BlockingQueueMultiStream {
  val DUMMY_OBJECT = new Object // type system issues regarding booleans
}

// Can have multiple producers and consumers.
class BlockingQueueMultiStream[T <: AnyRef](private val queue: BlockingQueue[T]) extends MultiStream[T] {
  import BlockingQueueMultiStream.DUMMY_OBJECT

  private var isShutdown = false
  private val getting = new ConcurrentHashMap[Thread, Object]()
  private val shutdownWatcher = new Object

  // Puts an item into the stream at an unspecified position.
  // Throws a StreamShutdownException if shutdown() has been called.
  def put(item: T): Unit = {
    // TODO: synchronization issue: producers that are about to put an item
    // into the queue after checking shutdown.

    if (isShutdown) {
      throw StreamShutdownException()
    } else {
      queue.put(item)
    }
  }
    
  // Gets an unspecified item from a stream.
  // If None is returned, it means the stream is empty.
  def get(): Option[T] = {
    val retval = queue.poll()

    if (retval ne null) {
      return Some(retval)
    }

    var placed = false

    try {
      shutdownWatcher.synchronized {
        if (!isShutdown) {
          val gettingRetval = getting.put(Thread.currentThread, DUMMY_OBJECT)
          assert(gettingRetval eq null)
          placed = true
        }
      }

      if (placed) {
        Some(queue.take())
      } else {
        None
      }
    } catch {
      case _: InterruptedException => None
    } finally {
      if (placed) {
        getting.remove(Thread.currentThread, DUMMY_OBJECT)
      }
    }
  }
  
  // Initiates a graceful shutdown of the stream.
  // -put(): All puts after shutdown will fail with a StreamShutdownException.
  //         All puts before will succeed.
  // -get(): Once the queue is empty, all gets after shutdown will return None.
  def shutdown() {
    import scala.collection.JavaConverters._

    shutdownWatcher.synchronized {
      isShutdown = true
    }

    getting.asScala.keys.foreach(_.interrupt())
  }
}

class BoundedMultiStream[T <: AnyRef](val size: Int)
extends BlockingQueueMultiStream[T](new ArrayBlockingQueue[T](size))

