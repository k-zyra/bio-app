package utils

/* External imports */
import scala.io.Source

/* Internal imports */
import bio.datatypes.File
import bio.datatypes.Sequence


object FileUtils {
	private val logger = new Logger("FileUtils")


    /**  Read given file
     *   Runs proper function, based on the file type
     *   If file type not supported, returns empty List 
     */
    def readFile(filename: String): File = {
        var file: File = Constants.EMPTY_FILE

        if (filename.endsWith(Constants.FASTQ_EXT)) {
            file = readFastqFile(filename)
        } else if (filename.endsWith(Constants.FASTA_EXT)) {
            file = readFastaFile(filename)
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
            val (content, rest) = lines.span(_(0) != StringUtils.toChar(Constants.HEADER_TAG))
            val (read, score) = content.span(_(0) != StringUtils.toChar(Constants.SEQID_TAG))
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
            val (content, rest) = lines.span(_(0) != StringUtils.toChar(Constants.HEADER_TAG))
            val (read, score) = content.span(_(0) != StringUtils.toChar(Constants.SEQID_TAG))
            val seq = new Sequence(name, read.mkString, score.mkString)
            
            (seq)::parse(rest)
        }

        val output = parse(Source.fromFile(filename).getLines())
        return new File(filename, "FASTQ", output.toArray)
    }


	/**  Analyze content of given file
     */
	def analyzeFile(filename: String, verbose: Boolean = false): Map[String, Int] = {
        var statistics = Map[String, Int]()
        var headers = 0
        var scores = 0

        var lines = Source.fromFile(filename).getLines().size
        for (line <- Source.fromFile(filename).getLines()) {
            if (line.startsWith(Constants.HEADER_TAG) | line.startsWith(">")) headers = headers + 1
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
        if (!filename.endsWith(Constants.FASTQ_EXT)) {
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
		if (header == Constants.EMPTY_STRING || !(header.startsWith(Constants.HEADER_TAG))) {
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
        return reads.filterNot(read => read.contains(Constants.AMBIGUOUS))
    }
}
