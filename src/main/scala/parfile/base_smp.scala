package parfile

import parfile.backend._

abstract class BaseSMP[T <: AnyRef](numProducers: Int, numConsumers: Int) {
  assert(numProducers > 0)
  assert(numConsumers > 0)

  protected val streamManager = new StreamManager(
    List.fill(numProducers)(producerMaker).toSeq,
    List.fill(numConsumers)(consumerMaker).toSeq,
    new BoundedMultiStream[T](1000))

  def start() {
    streamManager.runManager()
  }

  def producerMaker(): MultiStreamProducerInterface[T] => Producer[T]
  def consumerMaker(): MultiStreamConsumerInterface[T] => Consumer[T]
}
