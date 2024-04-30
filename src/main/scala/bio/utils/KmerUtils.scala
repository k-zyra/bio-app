package bio.utils

/* External imports */
import org.apache.spark.SparkContext

/* Internal imports */
import app.SparkController
import misc.{Constants, Logger}



object KmerUtils {
    private val logger = new Logger("KmerUtils")

    
    /** Get only kmers from array containing kmers with counters
     */
    def getKmers(kmersWithCounters: Array[(String, Int)]): Array[String] = {
        kmersWithCounters.map(_._1)
    }


    /** Get only counters from array containing kmers with counters
     */
    def getCounters(kmersWithCounters: Array[(String, Int)]): Array[Int] = {
        kmersWithCounters.map(_._2)
    }


    /** Get number of possible kmers
     *  Count how many kmers for given read and k value can be generated
     */
    def getNumberOfPossibleKmers(read: String, k: Integer): Integer = {
        val L = read.length()
        (L - k + 1)
    }


    /** Get number of kmers based on the given array
     *  Sum up second values of tuples which indicate how many kmers were found for given sequence
     */
    def getNumberOfKmers(kmers: Array[(String, Int)]): Integer = {
        kmers.foldLeft(0)((sum, kmer) => sum + kmer._2)
    }


    /** Prepare kmers to extract weak/solid kmers
     *  Internal use only.
     */
    private def _prepareKmers(seq: String,
                            k: Integer,
                            verbose: Boolean = logger.isVerbose()): Array[(String, Int)] = {
        val kmers = this.generateKmers(seq, k, verbose)
        this.countKmers(kmers, verbose = verbose)
    }


    /** Generate kmers
     *  Split read into kmers for given k value 
     */
    def generateKmers(read: String,
                    k: Integer,
                    verbose: Boolean = logger.isVerbose()): Seq[String] = {
        val start: Long = System.nanoTime()

        val L = read.length()
        val iterRange = Seq.range(0, L-k+1, 1)
        val kmersSeq = for (n <- iterRange) yield read.slice(n, n+k)
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) logger.logInfo(f"Time spent in <generateKmers> ${duration} ms")
        kmersSeq
    }


    /** Count kmers
     *  Calculate occurrences of each kmer in given array
     */
    def countKmers(kmers: Seq[String],
                   context: SparkContext = SparkController.getContext(),
                   verbose: Boolean = logger.isVerbose()): Array[(String, Int)] = {
        val start: Long = System.nanoTime()
        val rddKmers = context.parallelize(kmers).map(kmer => (kmer, 1))
        val countedKmers = rddKmers.reduceByKey(_ + _) 
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) logger.logInfo(f"Time spent in <countKmers> ${duration} ms")
        countedKmers.collect()
    }


    /** Prepare kmers for further steps
      *  Return array of tuples (occurrences, kmers)
      */
    def prepareAllKmersSequential(reads: Array[String],
                        k: Integer = Constants.ParameterUnspecified, 
                        verbose: Boolean = logger.isVerbose()): Array[(String, Int)] = {
        val start: Long = System.nanoTime()

        var kmerLength: Integer = k
        if (kmerLength == Constants.ParameterUnspecified) {
            kmerLength = (2 * FileUtils.getAverageReadLength(reads) / 3).toInt
            if (verbose) logger.logInfo(f"Setting k to value: ${kmerLength}")
        }

        var allKmers = Array[(String, Int)]()
        for (read <- reads) {
            var tempArray = this._prepareKmers(read, kmerLength, false) 
            allKmers ++= tempArray 
        }
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) logger.logInfo(f"Time spent in <prepareAllKmersSequential>: ${duration} ms")
        allKmers
    }


    /** Prepare kmers for further steps
     *  Return array of tuples (occurences, kmers)
     */
    def prepareAllKmers(reads: Array[String],
                        k: Integer = Constants.ParameterUnspecified,
                        context: SparkContext = SparkController.getContext(),
                        verbose: Boolean = logger.isVerbose()): Array[(String, Int)] = {
        val start: Long = System.nanoTime()
        var kmerLength: Integer = k
        if (kmerLength == Constants.ParameterUnspecified) {
            kmerLength = (2 * FileUtils.getAverageReadLength(reads) / 3).toInt
            if (verbose) logger.logInfo(f"Setting k to value: ${kmerLength}")
        }

        val readsPar = reads.par
        val allKmersPar = readsPar.flatMap(read => this.generateKmers(read, kmerLength, verbose=false))
        val allKmers = this.countKmers(allKmersPar.to[Seq], context, verbose=false)
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) logger.logInfo(f"Time spent in <prepareAllKmers>: ${duration} ms")
        allKmers
    }


    /** Get weak kmers for given sequence and k value
     *  Return 10% (if no different value given) of least frequent kmers 
     */
    def getWeakKmers(seq: String, k: Integer,
                    threshold: Integer = Constants.WeakKmerThreshold): Array[(String, Int)] = {
        val numberOfKmers: Integer = this.getNumberOfPossibleKmers(seq, k) 
        val counted = this._prepareKmers(seq, k)
        
        counted.sortBy(_._2)
        counted.take(numberOfKmers * threshold/100)
    }


    /** Get weak kmers for given sequence and k value
     *  Return 10% (if no different value given) of least frequent kmers 
     */
    def getSolidKmers(seq: String, k: Integer,
                    threshold: Integer = Constants.WeakKmerThreshold): Array[(String, Int)] = {
        val numberOfKmers: Integer = this.getNumberOfPossibleKmers(seq, k) 
        val counted = this._prepareKmers(seq, k)
        
        counted.sortBy(-_._2)
        counted.take(numberOfKmers * threshold/100)
    }


    /** Calculate kmer frequencies for given sequence
     *  Return array of kmers and their frequencies (occurences/total number of kmers) 
     */
    def countKmerFrequencies(seq: String, 
                            k: Integer,
                            verbose: Boolean = logger.isVerbose()): Array[(String, Float)] =  {
        val kmers = this.generateKmers(seq, k, verbose = false)
        val numberOfKmers: Float = kmers.length.toFloat
        val countedKmers = this.countKmers(kmers, verbose = verbose)

        countedKmers.map { case (kmer, counter) => (kmer, counter.toFloat/numberOfKmers) }
    }


    /** Get kmer spectra
     */
    def getKmerSpectra(seq: String,
                    k: Integer): Unit = {
        val frequencies = this.countKmerFrequencies(seq, k, verbose = false)
    }
}
