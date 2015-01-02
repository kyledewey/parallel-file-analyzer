package parfile.config

import java.io.File

object StaticSMPConfigFile {
  val NUM_CORES = "num_cores"
  val CONSUMER_BASE_COMMAND = "consumer_base_command"

  val splitCommand: String => Seq[String] =
    s => s.split(" ").toSeq

  def toNonNegativeInt(s: String): Int = {
    val base = s.toInt
    if (base < 0) {
      throw new NumberFormatException("Integer must be non-negative")
    }
    base
  }

  // Gives a positive value showing the number of cores
  def toNumCores(s: String): Int = {
    val base = toNonNegativeInt(s)
    if (base == 0) {
      Runtime.getRuntime.availableProcessors
    } else {
      base
    }
  }

  val converters: Map[String, String => Any] =
    Map(NUM_CORES -> (toNumCores _),
        CONSUMER_BASE_COMMAND -> splitCommand)

  val defaults: Map[String, Any] =
    // Specifiy 0 for the same number as the system has
    Map(NUM_CORES -> 0)
}

trait ConsumerBaseCommand extends ConfigFileInterface {
  lazy val consumerBaseCommand: Seq[String] =
    getEntry(StaticSMPConfigFile.CONSUMER_BASE_COMMAND).asInstanceOf[Seq[String]]
}

class StaticSMPConfigFile(file: File)
extends ConfigFile(file, StaticSMPConfigFile.converters, StaticSMPConfigFile.defaults) with ConsumerBaseCommand {
  lazy val numCores = 
    getEntry(StaticSMPConfigFile.NUM_CORES).asInstanceOf[Int]
}

object DynamicSMPConfigFile {
  import StaticSMPConfigFile._

  val PRODUCER_COMMAND = "producer_command"
  val CONSUMER_SAVE_FILE_TO = "consumer_save_file_to"
  val NUM_PRODUCERS = "num_producers"
  val NUM_CONSUMERS = "num_consumers"

  val converters: Map[String, String => Any] =
    Map(PRODUCER_COMMAND -> splitCommand,
        CONSUMER_SAVE_FILE_TO -> ConfigFile.id,
        CONSUMER_BASE_COMMAND -> splitCommand,
        NUM_PRODUCERS -> (toNumCores _),
        NUM_CONSUMERS -> (toNumCores _))

  val defaults: Map[String, Any] = 
    Map(NUM_PRODUCERS -> 1,
        NUM_CONSUMERS -> 0)
}

class DynamicSMPConfigFile(file: File)
extends ConfigFile(file, DynamicSMPConfigFile.converters, DynamicSMPConfigFile.defaults) with ConsumerBaseCommand {
  import DynamicSMPConfigFile._

  lazy val producerCommand: Seq[String] =
    getEntry(PRODUCER_COMMAND).asInstanceOf[Seq[String]]

  lazy val consumerSaveFileTo: String =
    getEntry(CONSUMER_SAVE_FILE_TO).asInstanceOf[String]

  lazy val numProducers: Int =
    getEntry(NUM_PRODUCERS).asInstanceOf[Int]

  lazy val numConsumers: Int =
    getEntry(NUM_CONSUMERS).asInstanceOf[Int]
}
