package parfile.config

import java.io.File

object StaticSMPConfigFile {
  val NUM_CORES = "num_cores"

  val converters: Map[String, String => Any] =
    Map(NUM_CORES -> ((s: String) => s.toInt))

  val defaults: Map[String, Any] =
    // Specifiy 0 for the same number as the system has
    Map(NUM_CORES -> 0)
}

trait NumCores extends ConfigFileInterface {
  import StaticSMPConfigFile._
  def numCores(): Int = getEntry(NUM_CORES).asInstanceOf[Int]
}

class StaticSMPConfigFile(file: File)
extends ConfigFile(file, StaticSMPConfigFile.converters, StaticSMPConfigFile.defaults) with NumCores

object DynamicSMPConfigFile {
  import ConfigFile.id

  val PRODUCER_COMMAND = "producer_command"
  val CONSUMER_BASE_COMMAND = "consumer_base_command"
  val CONSUMER_SAVE_FILE_TO = "consumer_save_file_to"

  val splitCommand: String => Seq[String] =
    s => s.split(" ").toSeq

  val converters: Map[String, String => Any] =
    Map(PRODUCER_COMMAND -> splitCommand,
        CONSUMER_BASE_COMMAND -> splitCommand,
        CONSUMER_SAVE_FILE_TO -> id) ++ StaticSMPConfigFile.converters

  val defaults: Map[String, Any] = StaticSMPConfigFile.defaults
}

class DynamicSMPConfigFile(file: File)
extends ConfigFile(file, DynamicSMPConfigFile.converters, DynamicSMPConfigFile.defaults) with NumCores {
  import DynamicSMPConfigFile._
  def producerCommand(): Seq[String] =
    getEntry(PRODUCER_COMMAND).asInstanceOf[Seq[String]]

  def consumerBaseCommand(): Seq[String] =
    getEntry(CONSUMER_BASE_COMMAND).asInstanceOf[Seq[String]]
}
