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

  val converters: Map[String, String => Any] =
    Map(NUM_CORES -> (toNonNegativeInt _),
        CONSUMER_BASE_COMMAND -> splitCommand)

  val defaults: Map[String, Any] =
    // Specifiy 0 for the same number as the system has
    Map(NUM_CORES -> 0)
}

trait NumCores extends ConfigFileInterface {
  lazy val numCores: Int = {
    val base = getEntry(StaticSMPConfigFile.NUM_CORES).asInstanceOf[Int]
    if (base == 0) {
      Runtime.getRuntime.availableProcessors
    } else {
      base
    }
  }
}

trait ConsumerBaseCommand extends ConfigFileInterface {
  lazy val consumerBaseCommand: Seq[String] =
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
  lazy val producerCommand: Seq[String] =
    getEntry(PRODUCER_COMMAND).asInstanceOf[Seq[String]]

  lazy val consumerSaveFileTo: String =
    getEntry(CONSUMER_SAVE_FILE_TO).asInstanceOf[String]
}
