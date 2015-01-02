package parfile

import java.io.File

import parfile.backend._
import parfile.config._

class DynamicSMP(producerCommand: Seq[String],
                 numProducers: Int,
                 consumerCommand: Seq[String],
                 numConsumers: Int,
                 whereToSave: String) extends BaseSMP[File](numProducers, numConsumers) {
  def producerMaker: MultiStreamProducerInterface[File] => Producer[File] =
    stream => new FileProducer(producerCommand, stream)

  def consumerMaker: MultiStreamConsumerInterface[File] => Consumer[File] = 
    stream => new FileConsumerSaveIfOutputNonEmpty(consumerCommand, stream, whereToSave)
}

object DynamicSMP {
  // First and only argument: configuration file
  def main(args: Array[String]) {
    if (args.length != 1) {
      println("Needs a configuration file")
    } else {
      val config = new DynamicSMPConfigFile(new File(args(0)))
      new DynamicSMP(config.producerCommand,
                     config.numProducers,
                     config.consumerBaseCommand,
                     config.numConsumers,
                     config.consumerSaveFileTo).start()
    }
  }
}

      
