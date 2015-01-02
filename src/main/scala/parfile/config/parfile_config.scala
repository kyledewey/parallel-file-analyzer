package parfile.config

import java.io.File

object StaticSMPConfigFile {
  val NUM_CORES = "num_cores"
  val CONSUMER_BASE_COMMAND = "consumer_base_command"

  val splitCommand: String => Seq[String] =
    s => s.split(" ").toSeq

  val converters: Map[String, String => Any] =
    Map(NUM_CORES -> ((s: String) => s.toInt),
        CONSUMER_BASE_COMMAND -> splitCommand)

  val defaults: Map[String, Any] =
    // Specifiy 0 for the same number as the system has
    Map(NUM_CORES -> 0)
}

trait NumCores extends ConfigFileInterface {
  def numCores(): Int =
    getEntry(StaticSMPConfigFile.NUM_CORES).asInstanceOf[Int]
}

trait ConsumerBaseCommand extends ConfigFileInterface {
  def consumerBaseCommand(): Seq[String] =
    getEntry(StaticSMPConfigFile.CONSUMER_BASE_COMMAND).asInstanceOf[Seq[String]]
}

class StaticSMPConfigFile(file: File)
extends ConfigFile(file, StaticSMPConfigFile.converters, StaticSMPConfigFile.defaults) with NumCores with ConsumerBaseCommand

object DynamicSMPConfigFile {
  val PRODUCER_COMMAND = "producer_command"
  val CONSUMER_SAVE_FILE_TO = "consumer_save_file_to"

  val converters: Map[String, String => Any] =
    Map(PRODUCER_COMMAND -> StaticSMPConfigFile.splitCommand,
        CONSUMER_SAVE_FILE_TO -> ConfigFile.id) ++ StaticSMPConfigFile.converters

  val defaults: Map[String, Any] = StaticSMPConfigFile.defaults
}

class DynamicSMPConfigFile(file: File)
extends ConfigFile(file, DynamicSMPConfigFile.converters, DynamicSMPConfigFile.defaults) with NumCores with ConsumerBaseCommand {
  import DynamicSMPConfigFile._
  def producerCommand(): Seq[String] =
    getEntry(PRODUCER_COMMAND).asInstanceOf[Seq[String]]

  def consumerSaveFileTo(): String =
    getEntry(CONSUMER_SAVE_FILE_TO).asInstanceOf[String]
}
