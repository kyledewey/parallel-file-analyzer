package parfile.config

import java.io.File

object ConfigHelpers {
  val splitCommand: String => Seq[String] =
    s => s.split(" ").toSeq

  def toNonNegativeInt(s: String): Int = {
    val base = s.toInt
    if (base < 0) {
      throw new NumberFormatException("Integer must be non-negative")
    }
    base
  }

  def asNumCores(base: Int): Int = {
    assert(base >= 0)
    if (base == 0) {
      Runtime.getRuntime.availableProcessors
    } else {
      base
    }
  }
}
import ConfigHelpers._

object StaticSMPConfigFile {
  val NUM_CORES = "num_cores"
  val CONSUMER_BASE_COMMAND = "consumer_base_command"

  val converters: Map[String, String => Any] =
    Map(NUM_CORES -> (toNonNegativeInt _),
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
    asNumCores(getEntry(StaticSMPConfigFile.NUM_CORES).asInstanceOf[Int])
}

object DynamicSMPConfigFile {
  val PRODUCER_COMMAND = "producer_command"
  val NUM_PRODUCERS = "num_producers"
  val NUM_CONSUMERS = "num_consumers"

  val converters: Map[String, String => Any] =
    Map(PRODUCER_COMMAND -> splitCommand,
        StaticSMPConfigFile.CONSUMER_BASE_COMMAND -> splitCommand,
        NUM_PRODUCERS -> (toNonNegativeInt _),
        NUM_CONSUMERS -> (toNonNegativeInt _))

  val defaults: Map[String, Any] = 
    Map(NUM_PRODUCERS -> 1,
        NUM_CONSUMERS -> 0)
}

class DynamicSMPConfigFile(file: File)
extends ConfigFile(file, DynamicSMPConfigFile.converters, DynamicSMPConfigFile.defaults) with ConsumerBaseCommand {
  import DynamicSMPConfigFile._

  lazy val producerCommand: Seq[String] =
    getEntry(PRODUCER_COMMAND).asInstanceOf[Seq[String]]

  lazy val numProducers: Int =
    asNumCores(getEntry(NUM_PRODUCERS).asInstanceOf[Int])

  lazy val numConsumers: Int =
    asNumCores(getEntry(NUM_CONSUMERS).asInstanceOf[Int])
}
