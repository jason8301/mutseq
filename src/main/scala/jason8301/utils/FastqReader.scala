package jason8301.utils

import java.io.{File, FileInputStream}
import java.util.zip.GZIPInputStream

class FastqReader(inp: File, param: Param) extends Iterator[FqRead]{
  val i = fileToIterator

  override def hasNext: Boolean = i.hasNext

  override def next(): FqRead = {
    val n1 = i.next()
    checkNextLine()
    val seq = i.next()
    checkNextLine()
    val n2 = i.next()
    checkNextLine()
    val qual = i.next()
    FqRead(n1, seq.toUpperCase, qual, param)
  }

  def checkNextLine() = if(!i.hasNext) throw new Exception("Wrong format of fastq")

  /**
    * Check whether input file extention end with ".gz" and wrap with gzipInputStream if needed.
    * @return LineIterator
    */
  def fileToIterator = {
    if ( inp.getName.toLowerCase.endsWith(".gz"))
      io.Source.fromInputStream(new GZIPInputStream(new FileInputStream(inp)))
    else io.Source.fromFile(inp)
  }.getLines()


}
object FastqReader {
  def apply(inp: File, param: Param) = new FastqReader(inp, param)
}
