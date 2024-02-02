package bio.builders

/* External imports */
import java.util.Arrays._

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types._
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.{Row, SparkSession}

import scala.util.Sorting._

/* Internal imports */
import app.SparkController

import utils.Constants
import utils.Logger
import utils.StringUtils
import scala.collection.parallel.ParSeq


object SuffixArrayBuilder {
    val logger: Logger = new Logger("SuffixArray")


    /**  Generate suffixes from given string 
     *   Return list of tuples where first element is the suffix id and second is a suffix
     */
    def generateSuffixesInParallel(str: String,
                                eof: String = Constants.DefaultSentinel,
                                verbose: Boolean = logger.isVerbose()): ParSeq[ParSeq[Char]] = {
        val inputString: String = str + eof
        val parallelString = inputString.par

        // Use map to create a collection of suffixes in parallel
        val start: Long = System.nanoTime()
        val suffixes = parallelString.zipWithIndex.map{ case (_, i) => parallelString.drop(i) }
        val returnVal = suffixes.toList

        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis
        if (verbose) logger.logInfo(suffixes.length + " suffixes generated in parallel in " + duration + " ms")

        return suffixes.toSeq
    }


    /**  Generate suffixes from given string 
     *   Returns list of tuples where first element is the suffix id and second is a suffix
     */
    def generateSuffixes(str: String,
                        eof: String = Constants.DefaultSentinel,
                        sorted: Boolean = false,
                        verbose: Boolean = logger.isVerbose()): Seq[(Int, String)] = {
        var word: String = str + eof
        var suffixes = Array[(Int, String)]()

        val start: Long = System.nanoTime()
        for(n : Int <- 0 to word.length-Constants.ArrayPadding) {
            suffixes :+= (n, word.drop(n))
        }
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) logger.logInfo(suffixes.length + " suffixes generated sequentially in " + duration + " ms")
        if (sorted) return suffixes.sortBy(_._2) 
        else return suffixes
    }


    /**  Create a suffix array from given string 
     *   Return dataset of tuples where first element is the suffix id and second is a suffix
     *   Can be unsorted.
     */
    def createFullSuffixArray(str: String,
                        eof: String = Constants.DefaultSentinel,
                        sorted: Boolean = false,
                        verbose: Boolean = logger.isVerbose()): Dataset[Row] = {

        var session = SparkController.getSession()
        var context = SparkController.getContext()

        val start: Long = System.nanoTime()
        var dataset = this.generateSuffixes(str, eof, sorted, verbose)

        val rddStr: RDD[(Int, String)] = context.parallelize(dataset)
        val suffixDf = session.createDataFrame(rddStr)
                        .withColumnRenamed("_1", "ID")
                        .withColumnRenamed("_2", "Suffix")
        
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis
        if (verbose) logger.logInfo("Full suffix array generated in " + duration + " ms")
        return suffixDf
    }


    /**  Create a compressed suffix array from given string 
     *   Returns an array of starting indexes of suffixes.
     *   Sorted by default.
     */
    def createCompressedSuffixArray(str: String,
                                eof: String = Constants.DefaultSentinel, 
                                verbose: Boolean = logger.isVerbose()): Seq[Int] = {

        val session = SparkController.getSession()

        val start: Long = System.nanoTime()
        var dataset = this.generateSuffixes(str, sorted=true)
        val compressedSa: Seq[Int] = dataset.map {case (firstElement, _) => firstElement}

        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis
        if (verbose) logger.logInfo("Compressed suffix array generated in " + duration + " ms")
        return compressedSa
    }


    /**  Generate an inverse suffix array from given suffix array 
     *   Returns an array of indexes taken from SA in original sequence order.
     *   Orginal (not compressed) suffix array should be given.
     */
    def createInverseSuffixArray(sa: Seq[Int]): Seq[(Int, Int)] = {
        var inverseSa = Seq[(Int, Int)]()
        for (id <- 0 to sa.length-Constants.ArrayPadding) {
            inverseSa :+= (sa(id), id)
        }
        
        return inverseSa.sortBy(_._1)
    }


    /**  Generate longest common prefix array from inverse suffix array 
     *   Returns an array of indexes taken from SA in original sequence order.
     */
    def createLongestCommonPrefixArray(isa: Seq[(Int, Int)], seq: String): Array[Int] = {
        var numberOfSuffixes: Int = isa.length
        var lcpArray: Array[Int] = Array.fill(numberOfSuffixes)(0)
        
        println(s"Number of suffixes: $numberOfSuffixes")
        for (id <- isa) {
            var currentSuffixId: Integer = id._2
            var nextSuffixId: Integer = currentSuffixId + 1
            
            println(s"current suf id: $currentSuffixId, nextsuffid: $nextSuffixId")

            if (nextSuffixId < numberOfSuffixes) {
                val resultOption: Option[(Int, Int)] = isa.find(pair => pair._2 == nextSuffixId)
                var currentSuffixStart: Integer = id._1
                var nextSuffixstart: Integer = resultOption.get._1            

                var firstSuffix: String = seq.drop(currentSuffixStart)
                var secondSuffix: String = seq.drop(nextSuffixstart)

                println(s"first sux: $firstSuffix, second suf: $secondSuffix")

                var lll = StringUtils.getLengthOfLongestCommonPrefix(firstSuffix, secondSuffix)
                println(s"lcp: $lll for suf: $currentSuffixId")
                lcpArray(currentSuffixId) = lll
            }
        }

        return lcpArray
    }


    /**  Squash (basic or already compressed) suffix array to smaller size 
     *   Preserve number of omitted indexes as decimal fraction (if any).
     *   It is not designed to save space, rather for quick tandem number estimation.
     */
    def squashSuffixArray(sa: Seq[Int]): Seq[Float] = {
        var squashedSa = Seq[Float]()
        var omitted: Float = 0
        var factor: Float = 10

        for (n <- 1 to sa.length-Constants.ArrayPadding) {
            if ((sa(n-1)-sa(n)) == 1) {
                println("squashing")
                omitted += 1
            } else {
                println("stop squashing")
                omitted = 0
                factor = 10
            }
        }

        return squashedSa
    }

}
