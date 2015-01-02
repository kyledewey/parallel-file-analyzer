package parfile.backend

import java.io.File

// Simply executes the file, discarding its output
class FileConsumerIgnoreOutput(
  command: Seq[String],
  stream: MultiStreamConsumerInterface[File]) extends FileConsumer(command, stream) {

  def consumeFileOutput(file: File, output: Seq[String]) {}
}

// Takes a place where to save the file.  Assumed to be a path to a directory.
abstract class FileConsumerSaveIfOutputMatches(
  command: Seq[String],
  stream: MultiStreamConsumerInterface[File],
  val whereToSave: String) extends FileConsumer(command, stream) {
  
  def outputMatches(output: Seq[String]): Boolean

  def consumeFileOutput(file: File, output: Seq[String]) {
    if (outputMatches(output)) {
      val wrapper = new CommandWrapper(
        Seq("cp", file.toString, whereToSave))
      wrapper.process.waitFor()
      wrapper.done()
    }
  }
}

class FileConsumerSaveIfOutputNonEmpty(
  command: Seq[String],
  stream: MultiStreamConsumerInterface[File],
  whereToSave: String) extends FileConsumerSaveIfOutputMatches(command, stream, whereToSave) {
  
  def outputMatches(output: Seq[String]): Boolean = output.nonEmpty
}
