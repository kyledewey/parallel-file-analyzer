package parfile

import java.io.File

import parfile.backend._
import parfile.config._

abstract class BaseSMP[T](numProducers: Int, numConsumers: Int) {
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

class StaticSMP(command: Seq[String],
                numConsumers: Int,
                files: Seq[String]) extends BaseSMP[File](1, numConsumers) {
  val indexedFiles = files.map(s => new File(s)).toIndexedSeq
  def producerMaker: MultiStreamProducerInterface[File] => Producer[File] =
    stream => new ListProducer[File](stream, indexedFiles)
  def consumerMaker: MultiStreamConsumerInterface[File] => Consumer[File] = 
    stream => new FileConsumerIgnoreOutput(command, stream)
}
    
object StaticSMP {
  // First argument: configuration file
  // Following arguments: files to process
  def main(args: Array[String]) {
    args.toList match {
      case configFile :: toProcess => {
        val config = new StaticSMPConfigFile(new File(configFile))
        new StaticSMP(config.consumerBaseCommand,
                      config.numCores,
                      toProcess.toSeq).start()
      }
      case _ => {
        println("Needs a configuration file and files to process")
      }
    }
  }
}
