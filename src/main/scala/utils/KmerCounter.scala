package utils

/* External imports */
import java.util.Arrays

import org.apache.commons.lang3.ArrayUtils;
import org.apache.spark.rdd._
import org.apache.spark.SparkContext

/* Internal imports */
import app.SparkController
import bio.datatypes.Sequence


object KmerCounter {
    private val logger = new Logger("KmerCounter")

    
    /** Get only kmers from array containing kmers with counters
     */
    def getKmers(kmersWithCounters: Array[(String, Int)]): Array[String] = {
        return kmersWithCounters.map(_._1).toArray
    }


    /** Get only counters from array containing kmers with counters
     */
    def getCounters(kmersWithCounters: Array[(String, Int)]): Array[Int] = {
        return kmersWithCounters.map(_._2).toArray
    }


    /** Get number of possible kmers
     *  Count how many kmers for given read and k value can be generated
     */
    def getNumberOfPossibleKmers(read: String, k: Integer): Integer = {
        var L = read.length()
        return (L - k + 1)
    }


    /** Get number of kmers based on the given array
     *  Sum up second values of tuples which indicate how many kmers were found for given sequence
     */
    def getNumberOfKmers(kmers: Array[(String, Int)]): Integer = {
        return kmers.foldLeft(0)((sum, kmer) => sum + kmer._2)
    }


    /** Prepare kmers to extract weak/solid kmers
     *  Internal use only.
     */
    private def _prepareKmers(seq: String,
                            k: Integer,
                            verbose: Boolean = logger.isVerbose()): Array[(String, Int)] = {
        val kmers = this.generateKmers(seq, k, verbose)
        return this.countKmers(kmers, verbose)
    }


    /** Generate kmers
     *  Split read into kmers for given k value 
     */
    def generateKmers(read: String,
                    k: Integer,
                    verbose: Boolean = logger.isVerbose()): Seq[String] = {
        val context = SparkController.getContext()

        val start = System.currentTimeMillis()

        val L = read.length()
        val iterRange = Seq.range(0, L-k+1, 1)
        var kmers = collection.mutable.ArraySeq[String]()
        var kmersSeq = for (n <- iterRange) yield read.slice(n, n+k)
        val duration = System.currentTimeMillis() - start

        if (verbose) logger.logInfo(s"Time spent in <generateKmers> $duration ms")
        return kmersSeq
    }


    /** Count kmers
     *  Calculate occurences of each kmer in given array
     */
    def countKmers(kmers: Seq[String], verbose: Boolean = logger.isVerbose()): Array[(String, Int)] = {
        val context = SparkController.getContext()

        val start = System.currentTimeMillis()
        val rddKmers = context.parallelize(kmers).map(kmer => (kmer, 1))
        val countedKmers = rddKmers.reduceByKey(_ + _) 
        val duration = System.currentTimeMillis() - start

        if (verbose) logger.logInfo(s"Time spent in <countKmers> $duration ms")
        return countedKmers.collect()
    }


    /** Prepare kmers for further steps
      *  Return array of tuples (occurences, kmers)
      */
    def prepareAllKmersSequential(reads: Array[String],
                        k: Integer = Constants.PARAMETER_UNSPECIFIED, 
                        verbose: Boolean = logger.isVerbose()): Array[(String, Int)] = {
        var start = System.currentTimeMillis()

        var kmerLength: Integer = k
        if (kmerLength == Constants.PARAMETER_UNSPECIFIED) {
            kmerLength = (2 * FileUtils.getAverageReadLength(reads) / 3).toInt
            if (verbose) logger.logInfo(s"Setting k to value: $kmerLength")
        }

        var allKmers = Array[(String, Int)]()
        for (read <- reads) {
            var tempArray = this._prepareKmers(read, kmerLength, false) 
            allKmers ++= tempArray 
        }
        var duration = System.currentTimeMillis() - start

        if (verbose) logger.logInfo(s"Time spent in <prepareAllKmers>: $duration ms")
        return allKmers
    }


    /** Prepare kmers for further steps
     *  Return array of tuples (occurences, kmers)
     */
    def prepareAllKmers(reads: Array[String],
                        k: Integer = Constants.PARAMETER_UNSPECIFIED, 
                        verbose: Boolean = logger.isVerbose()): Array[(String, Int)] = {
        var start = System.currentTimeMillis()
        var kmerLength: Integer = k
        if (kmerLength == Constants.PARAMETER_UNSPECIFIED) {
            kmerLength = (2 * FileUtils.getAverageReadLength(reads) / 3).toInt
            if (verbose) logger.logInfo(s"Setting k to value: $kmerLength")
        }

        var readsPar = reads.par 
        var allKmersPar = readsPar.flatMap(read => this.generateKmers(read, kmerLength, verbose=false))
        var allKmers = this.countKmers(allKmersPar.to[Seq], verbose=false)
        var duration = System.currentTimeMillis() - start

        if (verbose) logger.logInfo(s"Time spent in <prepareAllKmers>: $duration ms")
        return allKmers
    }


    /** Get weak kmers for given sequence and k value
     *  Return 10% (if no different value given) of least frequent kmers 
     */
    def getWeakKmers(seq: String, k: Integer,
                    threshold: Integer = Constants.WEAK_KMER_THRESHOLD): Array[(String, Int)] = {
        val numberOfKmers: Integer = this.getNumberOfPossibleKmers(seq, k) 
        var counted = this._prepareKmers(seq, k)
        
        counted.sortBy(_._2)
        return counted.take(numberOfKmers * threshold/100)
    }


    /** Get weak kmers for given sequence and k value
     *  Return 10% (if no different value given) of least frequent kmers 
     */
    def getSolidKmers(seq: String, k: Integer,
                    threshold: Integer = Constants.WEAK_KMER_THRESHOLD): Array[(String, Int)] = {
        val numberOfKmers: Integer = this.getNumberOfPossibleKmers(seq, k) 
        var counted = this._prepareKmers(seq, k)
        
        counted.sortBy(-_._2)
        return counted.take(numberOfKmers * threshold/100)
    }


    /** Calculate kmer frequencies for given sequence
     *  Return array of kmers and their frequencies (occurences/total number of kmers) 
     */
    def countKmerFrequencies(seq: String, 
                            k: Integer,
                            verbose: Boolean = logger.isVerbose()): Array[(String, Float)] =  {
        val kmers = this.generateKmers(seq, k, verbose = false)
        val numberOfKmers: Float = kmers.length.toFloat
        var countedKmers = this.countKmers(kmers, verbose)

        return countedKmers.map { case (kmer, counter) => (kmer, counter.toFloat/numberOfKmers) }
    }


    /** Get kmer spectra
     */
    def getKmerSpectra(seq: String,
                    k: Integer,
                    verbose: Boolean = logger.isVerbose()): Unit = {
        val frequencies = this.countKmerFrequencies(seq, k, verbose = false)
        // kmer spektra liczymy dla konkretnego k=? 
        // na osi x są czestotliwosci
        // na osi y sumujemy ilosc kmerow ktory wystapily x razy
    }


    /** Evaluate kmer
     *  Decides whether given kmer should be discarded due to low importance or high error rate
     */
    def evaluateKmer(kmer: String, kmers: Array[(String, Int)]): Unit = {

    }


    /** Filter kmers 
     *  Evaluate all kmers for the sequence
     *  Return an array containing filtered kmers
     */
    def filterKmers(): Unit = {

    }
}
