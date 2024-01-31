package utils

/* External imports */
import java.util.Dictionary
import java.util.HashMap

import com.github.vickumar1981.stringdistance.StringDistance._
import org.apache.spark.rdd._
import org.apache.spark.SparkContext
import scala.collection.Map
import scala.collection.parallel.ParMap
import scala.math

/* Internal imports */
import bio.datatypes.Sequence
import app.SparkController
import _root_.bio.datatypes.File


object MetricsUtils {
    private val logger = new Logger("MetricsUtils")


    /**  Calculate mean frequency for given k-mers
     */
    def calculateMean(kmers: Array[(String, Int)]): Float = {
        val numberOfKmers: Float = KmerUtils.getNumberOfKmers(kmers).toFloat
        val kmerFrequencies = kmers.map(kmer => kmer._2/numberOfKmers)

        return kmerFrequencies.sum/numberOfKmers
    }


    /**  Calculate standard deviation of frequency for given k-mers
     */
    def calculateStdDev(kmers: Array[(String, Int)]): Float = {
        val numberOfKmers: Float = KmerUtils.getNumberOfKmers(kmers).toFloat
        val meanFrequency: Float = this.calculateMean(kmers)
        val stdDevCounter: Float = kmers.map(kmer => math.pow(kmer._2 - meanFrequency, 2)).sum.toFloat

        return math.sqrt(stdDevCounter/numberOfKmers-1).toFloat
    }


    /**  Calculate frequency Z score for given kmer 
     *   Z-score indicates how many standard deviations is given kmer from the mean
     */
    def calculateZscore(kmer: String, kmers: Array[(String, Int)]): Float = {
        val kmerWithCounter = kmers.find(_._1 == kmer)
        if (kmerWithCounter.isEmpty) {
            logger.logWarn(f"Given kmer: $kmer not found!")
            return Constants.NOT_FOUND_F
        }   

        val kmerCounter: Float = kmerWithCounter.get._2.toFloat
        val numberOfKmers: Float = KmerUtils.getNumberOfKmers(kmers).toFloat

        val kmerFrequency: Float = kmerCounter/numberOfKmers
        val meanFrequency: Float = this.calculateMean(kmers)
        val stdDevFrequency: Float = this.calculateStdDev(kmers)

        return (kmerFrequency - meanFrequency)/stdDevFrequency
    }


    /**  Estimate depth coverage  
     *   Estimation is based on mean k-mer coverage and both k-mer and read length
     *   Internal use only.
     */
    private def _estimateCoverageDepth(): Float = {
        return 0
    }


    /**  Estimate genome length based on k-mer spectra
     */
    def estimateGenomeLength(file: File): Float = {
        val totalNumberOfBases: Float = file.getReads.map(_.length).sum
        val coverageDepth: Float = this._estimateCoverageDepth()    

        return totalNumberOfBases/coverageDepth
    }


	/**  Calculate GC-content for a sequence  
     */
    def getGcContent(seq: String, verbose: Boolean = logger.isVerbose()): Float = {
        val atcg: Float = seq.length.toFloat
        val gc: Float = seq.count(_ == 'C') + seq.count(_ == 'G') 

        return (gc/atcg).toFloat
    }


    /**  Calculate frequency of bases for a sequence  
     */
    def getBasesFrequency(base: Char, seq: String): Float = {
        var stdBase = base.toUpper
        if (!Constants.BASES.contains(stdBase)) {
            logger.logCriticalError(s"Incorrect base given: $base")
            return Constants.NOT_FOUND_F
        }

        val atcg: Float = seq.length()
        val baseCnt: Float = seq.count(_ == base)
        return baseCnt/atcg
    }
    

    /** Count bases
     *  Calculate number of each base in a sequence
     *  Return bases with counters in RDD of tuples(Char, Int)
     */
    def countBases(seq: String,
                verbose: Boolean = logger.isVerbose()): RDD[(Char, Int)] = {
        val context = SparkController.getContext()
        
        val start: Long = System.nanoTime()
        val rddSeq = context.parallelize(seq).map(kmer => (kmer, 1))
        val countedBases = rddSeq.reduceByKey(_ + _) 
        val duration: Float = (System.nanoTime()-start)/Constants.NANO_IN_MILLIS

        if (verbose) logger.logInfo(f"Time spent in <countBases>: $duration ms")
        return countedBases
    }


    /** Count bases
     *  Calculate number of each base in a sequence 
     *  Return bases with counters in a map 
     */
    def countBasesToMap(seq: String,
                verbose: Boolean = logger.isVerbose()): Map[Char, Int] = {
        return this.countBases(seq, verbose).collectAsMap()
    }
    

    /** Count bases using ParArray
     *  Calculate number of each base in a sequence
     *  Return bases with counters in a ParMap
     */
    def countBasesToPar(seq: String,
                verbose: Boolean = logger.isVerbose()): ParMap[Char, Int] = {
        val start: Long = System.nanoTime()
        val parallelSeq = seq.par
        val countedBases = parallelSeq
            .groupBy(identity)
            .mapValues(_.size)
        val duration: Float = (System.nanoTime() - start)/Constants.NANO_IN_MILLIS

        if (verbose) logger.logInfo(f"Time spent in <countBasesUsingPar>: $duration ms")
        return countedBases
    }


    /** Count bases frequency
     *  Calculate frequency of occurrence for each base in a sequence
     */
    def countBasesFrequencies(seq: String,
                            verbose: Boolean = logger.isVerbose()): RDD[(Char, Float)] = {
        val seqLen: Float = seq.length()
        var basesCounts = this.countBases(seq, verbose)

        val start: Long = System.nanoTime()  
        val basesFreq = basesCounts.map(base => (base._1, base._2/seqLen)) 
        val duration: Float = (System.nanoTime() - start)/Constants.NANO_IN_MILLIS

        if (verbose) logger.logInfo(f"Time spent in <countBasesFrequencies>: $duration ms")
        return basesFreq
    }


    /**  Calculate TF-IDF metrics
     *   TF-IDF - Term Frequency, Inverse Document Frequency   
     */
    def getTfIdf(str: String): Float = {
        var result: Float = 0
        return result
    }


    /**  Calculate Jaccard similarity between given substring and others 
     */
    def getJaccardSimilarityForKmer(id: Integer, 
                                strings: Seq[String], 
                                verbose: Boolean = logger.isVerbose()): RDD[Double] = {
        val context = SparkController.getContext()
        val kmer = strings(id)
        var rddStrings = context.parallelize(strings)

        val start: Long = System.nanoTime()  
        var metrics = rddStrings.map(element => Jaccard.score(element, kmer))
        val duration: Float = (System.nanoTime() - start)/Constants.NANO_IN_MILLIS

        if (verbose) logger.logInfo(f"Time spent in <getJaccardSimilarity>: $duration ms")
        return metrics
    }


    /**  Calculate Hamming distance between given substring and others
     */
    def getHammingDistanceForKmer(id: Integer, 
                            strings: Seq[String], 
                            verbose: Boolean = logger.isVerbose()): RDD[Int] = {
        val context = SparkController.getContext()
        val kmer = strings(id)
        var rddStrings = context.parallelize(strings)

        val start: Long = System.nanoTime()  
        var metrics = rddStrings.map(element => Hamming.distance(element, kmer))
        val duration: Float = (System.nanoTime() - start)/Constants.NANO_IN_MILLIS

        if (verbose) logger.logInfo(f"Time spent in <getHammingDistance>: $duration ms")
        return metrics
    }
}
