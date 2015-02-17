package parfile.backend

import java.io.File
import java.nio.file.{DirectoryStream, Files, FileSystems}

abstract class Producer[T <: AnyRef](stream: MultiStreamProducerInterface[T]) {
  // Produces an item.  Produces None if there is nothing
  // left to produce.  Returning None indicates that there
  // will never be anything left to produce
  def produce(): Option[T]

  def start() {
    var item: Option[T] = produce
    while (item.isDefined) {
      stream.put(item.get)
      item = produce
    }
  }
}

class ListProducer[T <: AnyRef](stream: MultiStreamProducerInterface[T],
                                list: IndexedSeq[T]) extends Producer(stream) {
  private val len = list.size
  private var index = 0

  def produce(): Option[T] = {
    if (index < len) {
      val retval = Some(list(index))
      index += 1
      retval
    } else {
      None
    }
  }
}

// -dir: Directory to iterative over
// -extension: extension for files of interest
//
// A thread-safe iterator over a directory
class DirectoryIterator(dir: String, extension: String) {
  private val stream = 
    Files.newDirectoryStream(
      FileSystems.getDefault.getPath(dir),
      extension)
  private val iterator = stream.iterator

  def nextFile(): Option[File] = {
    try {
      Some(iterator.next.toFile)
    } catch {
      case _: NoSuchElementException => {
	stream.close()
	None
      }
    }
  }
}

class FileGlobProducer(stream: MultiStreamProducerInterface[File],
		       dirName: String,
		       extension: String) extends Producer(stream) {
  private val iterator = new DirectoryIterator(dirName, extension)

  def produce(): Option[File] = {
    iterator.nextFile()
  }
}

abstract class Consumer[T <: AnyRef](stream: MultiStreamConsumerInterface[T]) {
  // Consumes an item.
  def consume(item: T): Unit

  def start() {
    var item = stream.get()
    while (item.isDefined) {
      consume(item.get)
      item = stream.get
    }
  }
}
