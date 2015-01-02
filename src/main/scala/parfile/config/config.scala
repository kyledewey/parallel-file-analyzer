package parfile.config

import java.io.File

case class InvalidEntryException(msg: String) extends Exception(msg)

object ConfigFile {
  val id: String => Any = s => s
}

trait ConfigFileInterface {
  protected val entries: Map[String, Any]

  def getEntry(key: String): Any = {
    entries.get(key).getOrElse(
      throw InvalidEntryException("No such entry with key: " + key))
  }
}
  
// Simple config file format:
//
// entry1: value1\n
// entry2: value2\n
//
// Whitespace surrounding entries and values is stripped (`trim()`)
// 
// @param file a file to parse in
// @param converters converts entries to some type.  If no conversion should be
//                   performed, then supply an identity function.
// @param defaults default parameters
//
// @exception InvalidEntryException if we parse in an entry which isn't represented
//            in converters, or if an entry is malformed, or if duplicate entries
//            have been found.
// 
class ConfigFile(val file: File,
                 val converters: Map[String, String => Any],
                 val defaults: Map[String, Any]) extends ConfigFileInterface {
  protected val entries = parseFile()

  // Returns None if the line was blank
  protected def parseLine(line: String): Option[(String, Any)] = {
    val trimmed = line.trim
    if (trimmed.length == 0) {
      None
    } else {
      val index = line.indexOf(':')
      if (index == -1) {
        throw InvalidEntryException("Line missing a colon: " + line)
      }
      if (index + 1 == line.length) {
        throw InvalidEntryException("Colon at end of line: " + line)
      }

      val key = line.substring(0, index).trim
      val value = line.substring(index + 1).trim

      if (key.length == 0) {
        throw InvalidEntryException("Key has empty length")
      }
      if (value.length == 0) {
        throw InvalidEntryException("Value has empty length")
      }

      if (!converters.contains(key)) {
        throw InvalidEntryException("Invalid key: " + key)
      }

      Some((key, converters(key)(value)))
    }
  }

  protected def parseFile(): Map[String, Any] = {
    import scala.io.Source

    val beforeDefaults =
      Source.fromFile(file).getLines().foldLeft(Map[String, Any]())((res, cur) =>
        parseLine(cur).map(
          { case (key, value) => {
              if (res.contains(key)) {
                throw InvalidEntryException("Duplicate key: " + key)
              }
            res + (key -> value)
          }
         }).getOrElse(res))
    
    defaults ++ beforeDefaults
  }
} // ConfigFile
