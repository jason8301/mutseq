package jason8301

import java.io.File
import java.util.Properties

import jason8301.mutseq.{FastqCrawler, FastqGenerator}
import jason8301.utils.Param

object MainApp extends App{
  val helpStr = """ Mutseq version 0.1
   usage: java -jar mutseq.jar <function> <options>
                 Supported functions: genseq, parseFq

          parseFq <fastq>
              parse the index splitted Fastq file <fastq> and generate two files that
              contains nucleotide and amino acid counts

          genseq <count>
              generate a Fastq with speficied numbers of reads

   """

  //val properties: Properties = getProperties()



  if(args.length > 2) {
    val fileToParse = new File(args(2))
    //val param = new PropReader(args(1)).getProp()
    val param = Param.genParam(args(1))
    args(0).toString.toLowerCase match {
      case "parsefq" => new FastqCrawler(fileToParse, param).parse()
      case "genseq" => new FastqGenerator(args(2).toInt, param)
      case "countoccu" => new FastqCrawler(fileToParse, param).countOccu()
      case "extractmut" => new FastqCrawler(fileToParse, param).extractMutFastq()
      case "getncount" => new FastqCrawler(fileToParse, param).nCount()
      case "aaoccu" => new FastqCrawler(fileToParse, param).aaOccu()
      case "frdpripos" => new FastqCrawler(fileToParse, param).frdPriPos(args)
      case "fixreaa" => new FastqCrawler(fileToParse, param).fixRegionAA(args)
      case "showprop" => System.err.println(Param.mkString(param))

      case _ => System.err.println(helpStr)

    }
  }
  else System.err.println(helpStr)

  
  
}
/*
object Parameters {

  val para_mutRegionLength = 21
  //val para_frdPriSeq = "ggttctgagaagatc".toUpperCase  //this is for Fyn
  val para_frdPriSeq = "GGGTTCTGGTGAGGGTTCT".toUpperCase  //this if for ZAP70

  //val para_overallQualityTestLength = 55
  val para_Ntransform_threshold = 5

  //para_preSeq is for FastqGenerator
  val para_preSeq = "CGATGTaagccgggttctggtgagggttctgagaagatc".toUpperCase
  //val para_postSeq = "GAGCTCTCTAAAGGTGAAG".toUpperCase  //this is for Fyn
  val para_postSeq = "AGTGGTGAGCTGGAGCT".toUpperCase   //This is for ZAP70
  val para_WTseq: String = "gagggcacgtacggcgtcgtg".toUpperCase

  val para_frdPrimerLeng: Int = para_frdPriSeq.length
  val para_postSeq2Cmp = para_postSeq.take(6).substring(1)

}
*/