package utils

/* External imports */
import app.SparkController
import org.apache.spark.rdd.RDD

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/* Internal imports */
import bio.datatypes.{File, Sequence}
import misc.{Constants, Logger}


object FileUtils {
	private val logger = new Logger("FileUtils")


    def getRoot(): String = {
        val currentDirectory: String = System.getProperty("user.dir");
        return currentDirectory
    }


    /**  Check whether given file is in FASTA format 
     */
    def isFasta(file: File): Boolean = {
        return file.getFileType().endsWith(Constants.FastaExtension)
    }


    /**  Check whether given file is in FASTQ format 
     */
    def isFastq(file: File): Boolean = {
        return file.getFileType().endsWith(Constants.FastqExtension)
    }


    /**  Check type for given filename
     */
    def checkFileType(filename: String): String = {
        val standardized: String = StringUtils.standardize(filename, upper=false)
        return "FASTA"
    }


    /**  Check type of given file
     */
    def checkFileType(file: File): String = {
        return "FASTA"
    }


    /** Read FASTA/FASTQ file into RDD of strings
     */
    def readAsRdd(filename: String): RDD[String] = {
        val context = SparkController.getContext()
        return context.textFile(filename)
    }


    /** Filter lines from a given file
     */
    def filterLines(filename: String,
                    startFrom: Int,
                    numberOfLines: Int): Array[String] = {
        val file = this.readAsRdd(filename)
        return file
          .zipWithIndex()
          .filter(_._2 % numberOfLines == startFrom)
          .map(_._1)
          .collect()
    }


    /** Get scores from a given file
     */
    def getScoresFromFile(filename: String): Array[String] = {
        val beginFrom: Int = 3
        var numberOfLines: Int = 0

        if (filename.endsWith(Constants.FastqExtension)) numberOfLines = 4
        else logger.logCriticalError(s"Not supported type of file: $filename!")

        return this.filterLines(filename, beginFrom, numberOfLines)
    }


    /** Get reads from a given file
     */
    def getReadsFromFile(filename: String): Array[String] = {
        val beginFrom: Int = 1
        var numberOfLines: Int = 0

        if (filename.endsWith(Constants.FastqExtension)) {
            numberOfLines = 4
        } else if (filename.endsWith(Constants.FastaExtension)) {
            numberOfLines = 2
        } else {
            logger.logCriticalError(s"Not supported type of file: $filename!")
        }

        return this.filterLines(filename, beginFrom, numberOfLines)
    }


    /** Get reads with associated scores from a given file
     */
    def getReadsAndScoresFromFile(filename: String): Array[(String, String)] = {
        val scores: Array[String] = this.getScoresFromFile(filename)
        val reads: Array[String] = this.getReadsFromFile(filename)
        return reads.zip(scores)
    }


    /**  Read given file
     *   Runs proper function, based on the file type
     *   If file type not supported, returns empty List 
     */
    def readFile(filename: String): File = {
        var file: File = Constants.EmptyFile

        if (filename.endsWith(Constants.FastqExtension)) {
            file = readFastqFile(filename)
        } else if (filename.endsWith(Constants.FastaExtension)) {
            file = readFastaFile(filename)
        } else if (filename.endsWith(Constants.TfaExtension)) {
            file = readTfaFile(filename)
        } else {
            logger.logCriticalError(s"Not supported type of file: $filename!")
        }

        return file
    }


	/**  Read given FASTA file
     */
    private def readFastaFile(filename: String): File = {
        def parse(lines: Iterator[String]): List[Sequence] = {
            if (lines.isEmpty) return List()

            val name = lines.next.drop(1)
            val (content, rest) = lines.span(_(0) != StringUtils.toChar(Constants.HeaderTag))
            val (read, score) = content.span(_(0) != StringUtils.toChar(Constants.SequenceIdTag))
            val seq = new Sequence(name, read.mkString, score.mkString)
            
            (seq)::parse(rest)
        }

        val output = parse(Source.fromFile(filename).getLines())
        return new File(filename, "FASTA", output.toArray)
    }


	/**  Read given FASTQ file
     */
	private def readFastqFile(filename: String): File = {
        def parse(lines: Iterator[String]): List[Sequence] = {
            if (lines.isEmpty) return List()

            val name = lines.next.drop(1)
            val (content, rest) = lines.span(_(0) != StringUtils.toChar(Constants.HeaderTag))
            val (read, score) = content.span(_(0) != StringUtils.toChar(Constants.SequenceIdTag))
            val seq = new Sequence(name, read.mkString, score.mkString)
            
            (seq)::parse(rest)
        }

        val output = parse(Source.fromFile(filename).getLines())
        return new File(filename, "FASTQ", output.toArray)
    }


    /** Read given TFA file
     */
    private def readTfaFile(filename: String): File = {
        val source: Source = Source.fromFile(filename)
        val readBuilder: StringBuilder = new StringBuilder(Constants.EmptyString)

        val headers: ArrayBuffer[String] = new ArrayBuffer[String]()
        val reads: ArrayBuffer[String] = new ArrayBuffer[String]()
        val output: ArrayBuffer[Sequence] = new ArrayBuffer[Sequence]()

        for (line <- source.getLines()) {
            if (line.startsWith(Constants.TfaHeaderTag)) {
                if (readBuilder.nonEmpty) {
                    reads += readBuilder.toString()
                    readBuilder.clear()
                }

                headers += line
            }
            else {
                readBuilder.append(line)
            }
        }
        reads += readBuilder.toString()
        readBuilder.clear()

        val readsWithHeaders: ArrayBuffer[(String, String)] = headers zip reads
        for (set <- readsWithHeaders.toArray) {
            output += new Sequence(set._1, set._2, Constants.EmptyString)
        }

        source.close()
        return new File(filename, "TFA", output.toArray)
    }


	/**  Analyze content of given file
     */
	def analyzeFile(filename: String, verbose: Boolean = false): Map[String, Int] = {
        var statistics = Map[String, Int]()
        var headers = 0
        var scores = 0

        val lines = Source.fromFile(filename).getLines().size
        for (line <- Source.fromFile(filename).getLines()) {
            if (line.startsWith(Constants.HeaderTag) | line.startsWith(">")) headers = headers + 1
            else if (line.startsWith("+")) scores = scores + 1          
        }

        statistics += ("lines" -> lines)
        statistics += ("headers" -> headers)
        statistics += ("scores" -> scores)

        if (verbose) logger.logInfo(s"Statistics for file [$filename]\n " +
                                    s"* Lines: $lines\n" +
                                    s"* Headers: $headers\n " + 
                                    s"* Scores: $scores") 
        return statistics 
    }


    /**  Print statistics for given file
     */
    def statistics(filename: String): Unit = {
        if (!filename.endsWith(Constants.FastqExtension)) {
            logger.logWarn(s"File $filename is not FASTQ file!")
            return
        }
        val fileStats = this.analyzeFile(filename)
        for ((k,v) <- fileStats) println(s"key: $k, value: $v")
    }


    /**  Parse headers from the following format:
     *          a:b:c:d:e:f:g:h:i:j:k
     *   where:
     *      a - unique instrument name
     *      b - run id
     *      c - flowcell id
     *      d - flowcell lane
     *      e - tile number within the flowcell lane
     *      f - x-coordinate of the cluster within the lane
     *      g - y-coordinate of the cluster within the lane
     */
	def parseReadHeader(header: String = "", verbose: Boolean = logger.isVerbose()): Unit = {
		if (header == Constants.EmptyString || !(header.startsWith(Constants.HeaderTag))) {
			logger.logCriticalError("No valid header given.")
			return
		}
		
		val segments : Array[String] = header.split(":")
		val instrumentName = segments(0)
		val runId = segments(1)
		val flowcellId = segments(2)
		val tileNumber = segments(3)
		val xCoordinate = segments(4)
		val yCoordinate = segments(5)

		if (verbose) logger.logInfo(s"Instrument name: $instrumentName\n" +
  							        s"Run ID: $runId\n" +
                                    s"Flowcell ID: $flowcellId\n" +
                                    s"Tile number: $tileNumber\n" +
                                    s"x coordinate: $xCoordinate\n" +
                                    s"y coordinate: $yCoordinate")
	}


    /**  Parse sequence ID from the following format:
     *          h:i:j:k
     *   where:
     *      h - the member of the pair (1/2)
     *      i - Y if the read fails filter (read is bad), N otherwise (read is OK)
     *      j - 0 when no control bits are on
     *      k - index sequence
     */
	def parseReadId(header: String = ""): Unit = {
		if (header == "") return
	}


    /**  Get average read length from given list of reads
     */
    def getAverageReadLength(reads: List[String]): Float = {
        val totalLength = reads.foldLeft(0)((acc, str) => acc + str.length)
        return (totalLength.toFloat)/(reads.length.toFloat)
    }


    /**  Get average read length from given array of reads
     */
    def getAverageReadLength(reads: Array[String]): Float = {
        val totalLength = reads.foldLeft(0)((acc, str) => acc + str.length)
        return (totalLength.toFloat)/(reads.length.toFloat)
    }


    /**  Filter ambiguous reads
     *   Remove all reads which contains N (non-defined base)
     *   Return array of filtered reads
     */
    def filterAmbiguousReads(reads: Array[String]): Array[String] = {
        return reads.filterNot(read => read.contains(Constants.Ambiguous))
    }
}
