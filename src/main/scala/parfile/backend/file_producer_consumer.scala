package parfile.backend

import java.io._

// Starts the given command
class CommandWrapper(val command: Seq[String]) {
  import scala.collection.JavaConverters._
  
  val process = runCommand()

  val processReader: BufferedReader = {
    new BufferedReader(
      new InputStreamReader(
        process.getInputStream))
  }

  protected def runCommand(): Process = {
    import scala.collection.JavaConverters._
    val retval = new ProcessBuilder(command.asJava)
    retval.redirectErrorStream(true)
    retval.start()
  }

  // Calls done() once we have finished reading the output
  def readOutputToCompletion(): Seq[String] = {
    import scala.collection.mutable.Buffer
    val retval = Buffer[String]()
    var line = processReader.readLine()
    while (line ne null) {
      retval += line
      line = processReader.readLine
    }
    done()
    retval.toSeq
  }

  def done() {
    processReader.close()
    process.getOutputStream.close()
    process.getErrorStream.close()
  }
}

// Executes the given command.  It is assumed that the given program
// writes files to some location and outputs their filenames, one per
// line.
class FileProducer(
  val command: Seq[String], 
  stream: MultiStreamProducerInterface[File]) extends Producer[File](stream) {

  protected val wrapper = new CommandWrapper(command)
  protected val reader = wrapper.processReader

  def finish() {
    wrapper.done()
  }

  def produce(): Option[File] = {
    val line = reader.readLine
    if (line eq null) {
      finish()
      None
    } else {
      Some(new File(line))
    }
  }
}

// Executes the given command, which is assumed to take a file in the
// last position.
abstract class FileConsumer(
  val command: Seq[String],
  stream: MultiStreamConsumerInterface[File]) extends Consumer(stream) {
  
  // Consumes the given file.  Automatically deletes it once done
  def consume(file: File) {
    val fileString = file.toString

    consumeFileOutput(
      file,
      new CommandWrapper(command ++ Seq(fileString)).readOutputToCompletion())

    if (!file.delete()) {
      println("WARNING: failed to delete file: '" + fileString + "'")
    }
  }

  // Take the file we are consuming along with the output of processing it
  def consumeFileOutput(file: File, output: Seq[String]): Unit
}
