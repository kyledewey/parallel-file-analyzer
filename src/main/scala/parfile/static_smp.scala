package parfile

import java.io.File

import parfile.backend._
import parfile.config._

class StaticSMP(command: Seq[String],
                numConsumers: Int,
                files: IndexedSeq[File]) extends BaseSMP[File](1, numConsumers) {
  def producerMaker: MultiStreamProducerInterface[File] => Producer[File] =
    stream => new ListProducer[File](stream, files)
  def consumerMaker: MultiStreamConsumerInterface[File] => Consumer[File] = 
    stream => new FileConsumer(command, stream)
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
                      toProcess.map(s => new File(s)).toIndexedSeq).start()
      }
      case _ => {
        println("Needs a configuration file and files to process")
      }
    }
  }
}
