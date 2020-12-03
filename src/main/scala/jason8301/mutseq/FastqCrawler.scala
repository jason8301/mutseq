package jason8301.mutseq

import java.io._

import scala.annotation.tailrec
import scala.collection.mutable
//import jason8301.Parameters._
import jason8301.utils.{FastqReader, FqRead, Param}

import scala.collection.parallel.immutable.ParSeq

/** The fastq parser. the input fastq should be a fastq file of one origin.
 *
 */
class FastqCrawler(inp: File, param: Param){

  val (validFq,notValid) = FastqReader(inp, param).partition(_.isValidFq)
  var validCnt = 0

  val nucContent = mutable.Map[String,Int]().withDefaultValue(0)
  val aaContent = mutable.Map[String,Int]().withDefaultValue(0)

  var sequenceCount =  mutable.Map[String, Int]()
  var aaSequenceCount =  mutable.Map[String, Int]()

  val mutAACountPerSeq = mutable.Map[Int,Int]()
  val mutNucCountPerSeq = mutable.Map[Int,Int]()

  lazy val WTAAseq = nucToAA(param.WTSeq)


  def parse() {
    System.out.println("Start parsing fastq file ..")

    for (fq <- validFq) {
      validCnt = validCnt + 1
      val mutRegion = fq.mutantRegionSeq

      addNucContent(mutRegion)
      addAAContent(mutRegion)
      addSeqCount(mutRegion)
      addAASeqCount(nucToAA(mutRegion))
      calculateMutPerSeq(mutRegion)
    }

    System.out.println("Done parsing.")
    outContentText()
    outSequenceCount()
    outStat()
  }

  /** extract the read seq and quality to a tsv format to stdout
    *
  */
  def extractMutFastq() {
    val out_mut = new BufferedWriter(new FileWriter(new File(inp.getAbsolutePath.concat(".MutRegion.txt"))))
    for (fq <- validFq) {
       out_mut.append(f"${fq.name}\t${fq.mutantRegionSeq}\t${fq.mutantRegionQual}")
    }
    out_mut.flush()
    out_mut.close()
  }

  def countOccu() { validFq.
    filter( _.isMutantRegionNLessThan(4) ).
    toList.par.groupBy(_.mutantRegionSeq).
    mapValues( getScoreEashPositiion(_)).
    foreach { (tpl: (String,  List[List[Int]])) => System.out.println(
        f"${tpl._1}%s\t${tpl._2.head.length.toString}%s\t${meanScoreEashPos(tpl._2)}%s\t${detailScoreEashPos(tpl._2)}%s"
    )}
  }

  def aaOccu() { validFq.
    filter( _.isMutantRegionNLessThan(4) ).
    toList.par.groupBy( (fq: FqRead) => nucToAA(fq.mutantRegionSeq)).
    mapValues(_.length).foreach {
    (tpl: (String, Int)) => System.out.println(f"${tpl._1}%s\t${tpl._2}%d")}
  }

  def nCount() {
    validFq.toList.par.groupBy(_.mutRegionNCount).mapValues(_.length).toList.foreach(
      (tpl: (Int, Int)) => System.out.println( f"${tpl._1}%d\t${tpl._2}%d" )
    )
  }

  def frdPriPos(args: Array[String]) {

    val fqs = (
      if( args.length > 2 ) {
        val st:Int = Integer.parseInt(args(3))
        val ed:Int = Integer.parseInt(args(4))
        validFq.filter((fq: FqRead) =>  nucToAA(fq.seq.substring(st,ed)).equalsIgnoreCase(args(2)))}
      else validFq).toList

    val resfqs = fqs.map(_.mutStart).groupBy( (i:Int) => i ).mapValues( _.length)
      resfqs.foreach(
      ( tpl:(Int, Int )) => System.err.println( f"${tpl._1}\t${tpl._2}")  )
      System.err.println(f"w/ Frd\t${resfqs.toList.map(_._2).sum}")

    if(args.length >2) {
      val fout = new BufferedWriter(new FileWriter( inp.getAbsolutePath.concat(f".${args(2)}.filteredseq.txt")))
      fqs.foreach( (fq:FqRead) => fout.append(fq.seq).append("\t").append(f"$fq.mutStart").append("\n") )
      fout.flush()
      fout.close()
    }
  }

  def fixRegionAA(args: Array[String]) {
    val st:Int = Integer.parseInt(args(2))
    val ed:Int = Integer.parseInt(args(3))
    validFq.map((fq: FqRead) => nucToAA(fq.seq.substring(st,ed)) ).foreach( System.out.println )
  }



  def getScoreEashPositiion(in: ParSeq[FqRead]): List[List[Int]] = {
    in.map(_.mutantRegionIntQual).toList.transpose
  }

  def detailScoreEashPos(in: List[List[Int]]): String =
    in.map(_.mkString(",")).mkString(";")


  def meanScoreEashPos(in: List[List[Int]]): String = {
    val count = in.head.length
    in.map( _.sum.toFloat / count ).mkString(",")
  }

  def getAAcontent = aaContent
  def getNucContent = nucContent



  def nucToAA(seq: String) = {
    @tailrec
    def helper(groupedSeq:List[String], accu:String): String =
      if(groupedSeq.isEmpty) accu
      else helper(groupedSeq.tail, accu ++ mapAA(groupedSeq.head) )
    helper(seq.grouped(3).toList, "")
  }

  def addNucContent(mutRegion: String) = addContent(zipToPosition(mutRegion).toList, nucContent)

  def addAAContent(mutRegion:String) = addContent(zipToPosition(nucToAA(mutRegion)).toList, aaContent)

  def zipToPosition(seq: String): IndexedSeq[String] =
    seq.zipWithIndex.map(tpl => f"${ tpl._2 }${tpl._1}" )

  def addContent(zipped:List[String], accu: mutable.Map[String,Int]) {
    for(toAdd <- zipped) { accu.+=( (toAdd, accu(toAdd)+1) )}
  }

  def addSeqCount(mutRegion: String) = {
    sequenceCount += ( (mutRegion, sequenceCount.getOrElse(mutRegion,0) +1 ))
  }

  def addAASeqCount(mutRegionAA: String) = {
    aaSequenceCount += ( (mutRegionAA, aaSequenceCount.getOrElse(mutRegionAA,0) +1 ))
  }

  def calculateMutPerSeq(mutRegion:String) = {
    val nucMutCnt = (param.WTSeq diff mutRegion).length
    val aaMutCnt = (WTAAseq diff nucToAA(mutRegion)).length

    mutNucCountPerSeq +=( (nucMutCnt, mutNucCountPerSeq.getOrElse(nucMutCnt,0) +1)  )
    mutAACountPerSeq +=( (aaMutCnt, mutAACountPerSeq.getOrElse(aaMutCnt,0) +1)  )

  }

  def mkNucContentText:String = {

    def genPosNucCont(pos: Int) = {
      f"${pos+1}\t" concat (for(c <- List('A','T','C','G')) yield f"$pos$c" ).map( nucContent(_).toDouble/validCnt ).mkString("\t")
    }

    "#Pos\tA\tT\tC\tG\n" concat
      (for( pos <- (0 to 20).toList ) yield genPosNucCont(pos)).mkString("\n")

  }

  def mkAAContentText:String = {
    def genPosAACont(pos: Int) = {
      f"${pos+1}\t" concat (for(c <- AAlist) yield f"$pos$c" ).map( aaContent(_).toDouble/validCnt).mkString("\t")
    }

    "#Pos\t" concat AAlist.mkString("\t") concat "\n" concat
      (for( pos <- (0 to 6).toList ) yield genPosAACont(pos)).mkString("\n")

  }


  def outContentText() {
    System.out.println("Generating output")
    val out_nuc = new BufferedWriter(new FileWriter(new File(inp.getAbsolutePath.concat(".NucFreq.txt"))))
    out_nuc.write(mkNucContentText)
    out_nuc.flush()
    out_nuc.close()

    val out_aa = new BufferedWriter(new FileWriter(new File(inp.getAbsolutePath.concat(".AAFreq.txt"))))
    out_aa.write(mkAAContentText)
    out_aa.flush()
    out_aa.close()
  }

  def outSequenceCount() {
    System.out.println("Generating sequences numbers")
    val out_top = new BufferedWriter(new FileWriter(new File(inp.getAbsolutePath.concat(".NucSeq.txt"))))
    out_top.write(sequenceCount.toSeq.sortBy(_._2).reverse.map( tpl => f"${tpl._1}\t${tpl._2}").mkString("\n"))
    out_top.flush()
    out_top.close()

    val out_aatop = new BufferedWriter(new FileWriter(new File(inp.getAbsolutePath.concat(".AASeq.txt"))))
    out_aatop.write(aaSequenceCount.toSeq.sortBy(_._2).reverse.map( tpl => f"${tpl._1}\t${tpl._2}").mkString("\n"))
    out_aatop.flush()
    out_aatop.close()
  }

  def outStat() {
    val out_sum = new BufferedWriter(new FileWriter(new File(inp.getAbsolutePath.concat(".Summary.txt"))))
    out_sum.append(f"Valid Fastq reads: $validCnt,\n")
    out_sum.append(f"\tDistinct Nucleotide sequence: ${sequenceCount.size},\n")
    out_sum.append(f"\tDistinct AA sequence: ${aaSequenceCount.size}\n")
    out_sum.append(findFailReason())
    out_sum.append("Nucleotide mutation per sequence:\n\t#mut\t#seq\n")
    out_sum.append(mutNucCountPerSeq.toSeq.sortBy(_._1).map( tpl => f"\t${tpl._1}\t${tpl._2}").mkString("\n"))
    out_sum.append("\n")
    out_sum.append("Amino acid mutation per sequence:\n\t#mut\t#seq\n")
    out_sum.append(mutAACountPerSeq.toSeq.sortBy(_._1).map( tpl => f"\t${tpl._1}\t${tpl._2}").mkString("\n"))
    out_sum.append("\n")
    out_sum.flush()
    out_sum.close()
  }

  def findFailReason(): String = {
    var nonFrdSeq: Int = 0
    var nonOverallConfident: Int = 0
    var nonMutationRegionConfident : Int = 0
    var nontacInMutantRegion: Int = 0
    var nonRevPrimerCorrectPosition : Int = 0
    var notValidCnt: Int = 0
    var notLongenough: Int = 0

    for( fq <- notValid) {
      notValidCnt = notValidCnt + 1
      if(!fq.containsFrdSeq) { nonFrdSeq = nonFrdSeq+1 }
      if(!fq.isSeqLengthCoverAllLength) {notLongenough = notLongenough +1}
      else {
        if (!fq.isOverallConfident) {
          nonOverallConfident = nonOverallConfident + 1
        }
        if (!fq.isMutationRegionConfident) {
          nonMutationRegionConfident = nonMutationRegionConfident + 1
        }
        if (!fq.tacInMutantRegion) {
          nontacInMutantRegion = nontacInMutantRegion + 1
        }
        if (!fq.isRevPrimerCorrectPosition) {
          nonRevPrimerCorrectPosition = nonRevPrimerCorrectPosition + 1
        }
      }
    }

    f"Invalid Reads: $notValidCnt\n" concat
      f"\tNot Containing frd seq: $nonFrdSeq\n" concat
      f"\tSeq region not long enough:  $notLongenough\n" concat
      f"\t\tNot confident overall: $nonOverallConfident\n" concat
      f"\t\tNot confident mutation region: $nonMutationRegionConfident\n" concat
      f"\t\tNo TAC in mutation region: $nontacInMutantRegion\n" concat
      f"\t\tMutation region is not 21nt long: $nonRevPrimerCorrectPosition\n\n"
  }



  val mapAA = Map(
    "TTT" -> "F", "TTC" -> "F", "TTA" -> "L", "TTG" -> "L",
    "TCT" -> "S", "TCC" -> "S", "TCA" -> "S", "TCG" -> "S",
    "TAT" -> "Y", "TAC" -> "Y", "TAA" -> ".", "TAG" -> ".",
    "TGT" -> "C", "TGC" -> "C", "TGA" -> ".", "TGG" -> "W",
    "CTT" -> "L", "CTC" -> "L", "CTA" -> "L", "CTG" -> "L",
    "CCT" -> "P", "CCC" -> "P", "CCA" -> "P", "CCG" -> "P",
    "CAT" -> "H", "CAC" -> "H", "CAA" -> "Q", "CAG" -> "Q",
    "CGT" -> "R", "CGC" -> "R", "CGA" -> "R", "CGG" -> "R",
    "ATT" -> "I", "ATC" -> "I", "ATA" -> "I", "ATG" -> "M",
    "ACT" -> "T", "ACC" -> "T", "ACA" -> "T", "ACG" -> "T",
    "AAT" -> "N", "AAC" -> "N", "AAA" -> "K", "AAG" -> "K",
    "AGT" -> "S", "AGC" -> "S", "AGA" -> "R", "AGG" -> "R",
    "GTT" -> "V", "GTC" -> "V", "GTA" -> "V", "GTG" -> "V",
    "GCT" -> "A", "GCC" -> "A", "GCA" -> "A", "GCG" -> "A",
    "GAT" -> "D", "GAC" -> "D", "GAA" -> "E", "GAG" -> "E",
    "GGT" -> "G", "GGC" -> "G", "GGA" -> "G", "GGG" -> "G",
    "CTN" -> "L", "GUN" -> "V", "UCN" -> "S", "CCN" -> "P",
    "ACN" -> "T", "GCN" -> "A", "CGN" -> "R", "GGN" -> "G"
  ).withDefaultValue("-")

  val AAlist = List('G','A','V','L','I','M','F','W','P','S','T','C','Y','N','Q','D','E','K','R','H')
}




