package jason8301.utils

//import jason8301.Parameters._

class FqRead(val name: String, val seq: String, val score: String, val param: Param) {

  def containsFrdSeq = seq.contains(param.frdPriSeq)

  def isSeqLengthCoverAllLength = (mutStart + param.mutRegionLength + 7) < seq.length //to filter reads that didn't have all mutation region sequenced

  def isOverallConfident = score.substring(3, mutStart + param.mutRegionLength + 6).forall(_.toInt > 48) //Q>15
  //def isOverallConfident = score.substring(3,30).forall( _.toInt > 48) //Q>15

  def isMutationRegionConfident = {
    score.substring(mutStart, mutStart + param.mutRegionLength).forall(_.toInt > 53) //Q>20
  }

  def tacInMutantRegion = seq.substring(mutStart + 9).take(3).equalsIgnoreCase("tac")

  def isRevPrimerCorrectPosition = {
    seq.substring(mutStart + param.mutRegionLength).take(6).substring(1).equalsIgnoreCase(param.postSeq2Cmp)
  }

  lazy val mutRegionNCount = mutantRegionSeq.toUpperCase.toList.count(_ == 'N')

  def isMutantRegionNLessThan(c: Int) = mutRegionNCount < c

  def isValidFq = containsFrdSeq && isSeqLengthCoverAllLength &&
    isOverallConfident && isMutationRegionConfident &&
    tacInMutantRegion && isRevPrimerCorrectPosition



  /** utils */
  lazy val mutStart = seq.indexOf(param.frdPriSeq) + param.frdPriSeq.length

  lazy val mutantRegionSeq = seq.substring(mutStart).take(param.mutRegionLength)

  lazy val mutantRegionQual = score.substring(mutStart).take(param.mutRegionLength)

  lazy val mutantRegionIntQual = mutantRegionQual.toList.map(_.toInt - 33)

  def trim(i:Int) = FqRead(name, seq.substring(i), score.substring(i), param)
  def trim(st:Int,ed:Int) = FqRead(name, seq.substring(st,ed), score.substring(st,ed), param)

  def cropReadBySeq(crop: String): FqRead = {
      val idx = seq.indexOf(crop)
      if (idx < 0) throw new StringIndexOutOfBoundsException
      else FqRead(name, crop, score.substring(idx), param)
  }

  def cropScoreBySeq(crop: String): String = {
      val idx = seq .indexOf(crop)
      if (idx < 0 ) throw new StringIndexOutOfBoundsException
      else score.substring(idx, idx+crop.length)
  }

  override def toString = "%s\n%s\n+\n%s\n".format(name, seq, score)

}
object FqRead {
  def apply(name: String, seq: String, score: String, param: Param) = new FqRead(name,seq,score, param)
  def toString(name: String, seq: String, score: String) = "%s\n%s\n+\n%s\n".format(name, seq, score)
  def toString(read: FqRead) = "%s\n%s\n+\n%s\n".format(read.name, read.seq, read.score)
  def reverseComp(seq: String): String = seq.reverse.map(compMap)
  val compMap = Map('A'->'T', 'C' -> 'G', 'G'-> 'C', 'T'-> 'A','N'->'N')
}