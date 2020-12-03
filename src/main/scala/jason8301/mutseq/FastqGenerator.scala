package jason8301.mutseq

import jason8301.utils.Param
//import jason8301.Parameters._
/**
 * Created by soom on 2015/7/29.
 */
class FastqGenerator(limit: Int, param: Param) {
  import scala.annotation.tailrec


  val rand = new java.util.Random()
  val nucleotides = Map(1 -> 'A', 2 -> 'T', 3 -> 'C', 0 -> 'G')
  val complement = Map('A'->'T', 'T'->'A','C'->'G','G'->'C')

  /** this map contains mostly J than else, to mimic the fred score */
  val fred = (List('D','E','F','G','H','I','I','I') ++ List.fill(45)('J')).
    zipWithIndex.map(_.swap).toMap

  /** this map contains 5% of 0, 90% of 1, 4% of 2 and 1% of 3 */
  val mutFreq = (List.fill(5)(0) ++ List.fill(90)(1) ++ List.fill(4)(2) ++ List(3)).
    zipWithIndex.map(_.swap).toMap
  def genReadName = {
    f"@EAS222:333:CD133GJ:1:${rand.nextInt(5096)}:${rand.nextInt(131072)}:${rand.nextInt(131072)} 1:Y:18:AATTGG"
  }

  /** generate list of positions to be mutate  */
  def genPosList(num: Int): List[Int] = {
    @tailrec
    def genMutPos: Int = { //the position can not reside in the "TAC" region
    val i = rand.nextInt(21)
      if (i < 9 || i > 11) i
      else genMutPos
    }
    for (i <- (1 to num).toList) yield genMutPos
  }

  /** the mutant nucleotide cannot be the same as wild type */
  @tailrec
  private def getMutChar(c: Char) : Char = {
    val randC = nucleotides(rand.nextInt(4))
    if(randC != c ) randC
    else getMutChar(c)
  }

  /** actual genetor funciton for mutant sequence */
  def genMutant = {
    val pos = genPosList( mutFreq(rand.nextInt(100)) )  //first decide amount and position of point mutations
    val sb = new StringBuilder(param.WTSeq)
    for (i <- pos) {
      sb.setCharAt(i, getMutChar( sb.charAt(i)))    //
    }
    //println(f"${sb.toString()}, pos ${pos}")
    sb.toString()
  }

  /** generic function for generating random sequence or score by length */
  def genStr(length: Int, typ: Map[Int, Any]):String = {
    for(i <- (1 to length).toList) yield typ(rand.nextInt(typ.size))
  }.mkString

  def reverseComp(seq: String) = seq.reverse map complement

  /* if Reverse Complement needed
  def genSeq = if(rand.nextBoolean()) f"${para_preSeq}${genMutant}${para_postSeq}"
  else reverseComp(f"${para_preSeq}${genMutant}${para_postSeq}")
  */
  def genSeq = f"${param.frdPriSeq}${genMutant}${param.postSeq}"

  /* old version, generate random sequece
  def genFastq_old(): String = { //generate 102 bp
    List(
      genReadName,
      f"${preSeq}${genStr(9, nucleotides)}TAC${genStr(9,nucleotides)}${postSeq}",
      "+",
      genStr(102,fred)
    ).mkString("\n")
  }*/

  def genFastq(): String = { //generate 102 bp
    List(
      genReadName,                         //read name
      genSeq,  //sequence
      "+",                                 //optional read name
      genStr(102,fred)                     //fred score
    ).mkString("\n")
  }


  var num = 0
  //val limit = args(0).toInt
  //val limit =5
  while( num < limit ){
    println(genFastq)
    num = num + 1
  }
}
