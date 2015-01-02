package parfile.backend

abstract class Producer[T](stream: MultiStreamProducerInterface[T]) {
  // Produces an item.  Produces None if there is nothing
  // left to produce.  Returning None indicates that there
  // will never be anything left to produce
  def produce(): Option[T]

  def start() {
    var item: Option[T] = produce
    while (item.isDefined) {
      stream.put(item.get)
      item = produce
    }
  }
}

class ListProducer[T](stream: MultiStreamProducerInterface[T],
                      list: IndexedSeq[T]) extends Producer(stream) {
  private val len = list.size
  private var index = 0

  def produce(): Option[T] = {
    if (index < len) {
      val retval = Some(list(index))
      index += 1
      retval
    } else {
      None
    }
  }
}

abstract class Consumer[T](stream: MultiStreamConsumerInterface[T]) {
  // Consumes an item.
  def consume(item: T): Unit

  def start() {
    var item = stream.get()
    while (item.isDefined) {
      consume(item.get)
      item = stream.get
    }
  }
}
