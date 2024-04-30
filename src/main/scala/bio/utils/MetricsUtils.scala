package bio.utils

/* External imports */
import com.github.vickumar1981.stringdistance.StringDistance._

import org.apache.spark.rdd._

import scala.collection.Map
import scala.collection.parallel.ParMap

/* Internal imports */
import app.SparkController
import bio.datatypes.File
import misc.{Constants, Logger}



object MetricsUtils {
    private val logger = new Logger("MetricsUtils")


    /** Estimate total coverage
     *  To estimate total coverage, genome size must be specified by an user
     */
    def estimateTotalCoverage(file: File,
                            genomeSize: Double,
                            precision: Int = 2,
                            verbose: Boolean = logger.isVerbose): Double = {
        val numberOfBases: Double = this.getTotalNumberOfBases(file).toDouble

        if (verbose) logger.logInfo(f"Genome size: ${genomeSize}, number of bases: ${numberOfBases}")
        BigDecimal(numberOfBases/genomeSize).setScale(precision, BigDecimal.RoundingMode.HALF_UP).toDouble
    }


    /** Get number of all bases, without any distinction
     *  Return total number in the given file
     */
    def getTotalNumberOfBases(file: File): Long = {
        val reads: Array[String] = file.getReads()

        val readsPar = reads.par
        val numberOfBases: Long = readsPar.map(_.length).sum
        println("Number of bases in whole file: " + numberOfBases)

        numberOfBases
    }


	/**  Calculate GC-content for a sequence  
     */
    def getGcContent(seq: String): Float = {
        val atcg: Float = seq.length.toFloat
        val gc: Float = seq.count(_ == 'C') + seq.count(_ == 'G') 

        (gc/atcg)
    }


    /**  Calculate frequency of bases for a sequence  
     */
    def getBasesFrequency(base: Char, seq: String): Float = {
        val stdBase = base.toUpper
        if (!Constants.Nucleobases.contains(stdBase)) {
            logger.logCriticalError(s"Incorrect base given: $base")
            return Constants.NotFoundFloat
        }

        val atcg: Float = seq.length()
        val baseCnt: Float = seq.count(_ == base)
        baseCnt/atcg
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
        val duration: Float = (System.nanoTime()-start)/Constants.NanoInMillis

        if (verbose) logger.logInfo(f"Time spent in <countBases>: $duration ms")
        countedBases
    }


    /** Count bases
     *  Calculate number of each base in a sequence 
     *  Return bases with counters in a map 
     */
    def countBasesToMap(seq: String,
                verbose: Boolean = logger.isVerbose()): Map[Char, Int] = {
        this.countBases(seq, verbose).collectAsMap()
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
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) logger.logInfo(f"Time spent in <countBasesUsingPar>: $duration ms")
        countedBases
    }


    /** Count bases frequency
     *  Calculate frequency of occurrence for each base in a sequence
     */
    def countBasesFrequencies(seq: String,
                            verbose: Boolean = logger.isVerbose()): RDD[(Char, Float)] = {
        val seqLen: Float = seq.length()
        val basesCounts = this.countBases(seq, verbose)

        val start: Long = System.nanoTime()  
        val basesFreq = basesCounts.map(base => (base._1, base._2/seqLen)) 
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) logger.logInfo(f"Time spent in <countBasesFrequencies>: $duration ms")
        basesFreq
    }


    /**  Calculate mean frequency for given k-mers
     */
    def calculateMean(kmers: Array[(String, Int)]): Float = {
        val numberOfKmers: Float = KmerUtils.getNumberOfKmers(kmers).toFloat
        val kmerFrequencies = kmers.map(kmer => kmer._2/numberOfKmers)

        kmerFrequencies.sum/numberOfKmers
    }


    /**  Calculate standard deviation of frequency for given k-mers
     */
    def calculateStdDev(kmers: Array[(String, Int)]): Float = {
        val numberOfKmers: Float = KmerUtils.getNumberOfKmers(kmers).toFloat
        val meanFrequency: Float = this.calculateMean(kmers)
        val stdDevCounter: Float = kmers.map(kmer => math.pow(kmer._2 - meanFrequency, 2)).sum.toFloat

        math.sqrt(stdDevCounter/numberOfKmers-1).toFloat
    }


    /**  Calculate frequency Z score for given kmer 
     *   Z-score indicates how many standard deviations is given kmer from the mean
     */
    def calculateZscore(kmer: String, kmers: Array[(String, Int)]): Float = {
        val kmerWithCounter = kmers.find(_._1 == kmer)
        if (kmerWithCounter.isEmpty) {
            logger.logWarn(f"Given kmer: $kmer not found!")
            return Constants.NotFoundFloat
        }   

        val kmerCounter: Float = kmerWithCounter.get._2.toFloat
        val numberOfKmers: Float = KmerUtils.getNumberOfKmers(kmers).toFloat

        val kmerFrequency: Float = kmerCounter/numberOfKmers
        val meanFrequency: Float = this.calculateMean(kmers)
        val stdDevFrequency: Float = this.calculateStdDev(kmers)

        (kmerFrequency - meanFrequency)/stdDevFrequency
    }


    /**  Calculate Jaccard similarity between given substring and others 
     */
    def getJaccardSimilarityForKmer(id: Integer, 
                                strings: Seq[String], 
                                verbose: Boolean = logger.isVerbose()): RDD[Double] = {
        val context = SparkController.getContext()
        val kmer = strings(id)
        val rddStrings = context.parallelize(strings)

        val start: Long = System.nanoTime()  
        val metrics = rddStrings.map(element => Jaccard.score(element, kmer))
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) logger.logInfo(f"Time spent in <getJaccardSimilarity>: $duration ms")
        metrics
    }


    /**  Calculate Hamming distance between given substring and others
     */
    def getHammingDistanceForKmer(id: Integer, 
                            strings: Seq[String], 
                            verbose: Boolean = logger.isVerbose()): RDD[Int] = {
        val context = SparkController.getContext()
        val kmer = strings(id)
        val rddStrings = context.parallelize(strings)

        val start: Long = System.nanoTime()  
        val metrics = rddStrings.map(element => Hamming.distance(element, kmer))
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) logger.logInfo(f"Time spent in <getHammingDistance>: $duration ms")
        metrics
    }
}
