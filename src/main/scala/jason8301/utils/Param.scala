package jason8301.utils

import java.io.File
import java.io.FileInputStream

case class Param(val WTSeq: String, val postSeq:String, val frdPriSeq: String, val mutRegionLength: Int, val postSeq2Cmp:String)
object Param {
  def genParam(path: String): Param = {
    val propFile = new File(path)

    if(!propFile.canRead) throw new IllegalArgumentException("Cannot read supplied conf file" + path)

    val paramMap = io.Source.fromFile(path).getLines.
      filter((s: String) => !s.startsWith("#")).
      filterNot(_ == ' ').
      map( (s:String) => {val sub = s.split("="); (sub(0).toLowerCase, sub(1).toUpperCase)} ).toMap

    val allGood: Boolean = List("wtseq","postseq", "frdpriseq").forall(paramMap.contains(_) )

    if(!allGood) throw new IllegalArgumentException("WTSeq, postSeq and frdPriSeq must all present in the conf file")

    new Param(WTSeq = paramMap("wtseq"), postSeq = paramMap("postseq"),
      frdPriSeq = paramMap("frdpriseq"), mutRegionLength = paramMap("wtseq").length ,
      postSeq2Cmp = paramMap("postseq").take(6).substring(1))

  }
  def mkString(param: Param): String = {
    "  WTSeq = %s\n  frdPriSeq = %s\n  postSeq = %s\n  mutRegionLength = %d\n  postSeq2Cmp = %s".format(
      param.WTSeq, param.frdPriSeq, param.postSeq, param.mutRegionLength, param.postSeq2Cmp )

  }
}