package app
 
/* External imports */ 
import java.nio.file.Paths
import java.io.File

import org.apache.spark.rdd.PairRDDFunctions
import org.apache.spark.SparkConf
import org.apache.spark.sql.{Row, SparkSession}

import scala.io.Source

/* Internal imports */ 
import bio.builders.SuffixArrayBuilder

import bio.ukkonen.UkkonenController
import bio.ukkonen.UkkonenEdge
import bio.ukkonen.UkkonenNode

import examples.BwtExample
import examples.LocalAlignmentExample

import utils._
import bio.clustering.BasicCluster
import bio.clustering.KmeansCluster
import bio.searchers.AlignSearcher
import org.apache.spark.SparkContext

import org.apache.log4j.Logger
import org.apache.log4j.Level
import examples.KmerOperationsExample
import org.apache.spark.sql.Dataset



object BioApp {
	def main(args: Array[String]): Unit = {
    	// var arguments = utils.OptionParser.parseArguments(args)
 
        val session = SparkController.getSession()
        val context = SparkController.getContext()

		// val fastqFile = "C:\\Users\\karzyr\\Desktop\\pacbio.fastq"
		// FileUtils.statistics(fastqFile)

		// val fastqContent = FileUtils.readFile(fastqFile)
		// println(fastqContent)

        // val sequences = fastqContent.getSequences()
        // val reads = fastqContent.getReads()


        // val realParKmers = allkmersPar.par
        // val start = System.currentTimeMillis()
        // val allSufsPar = realParKmers.map(kmer => SuffixArrayBuilder.generateSuffixes(kmer._1, verbose = false))
        // val duration = System.currentTimeMillis()  - start
        // println(f"Duration for parallel version: $duration")

        // val anotherStart = System.currentTimeMillis()
        // for (kmer <- allkmersPar) {
        //     SuffixArrayBuilder.generateSuffixes(kmer._1, verbose = false)
        // }
        // val anotherDuration = System.currentTimeMillis()  - anotherStart
        // println(f"Duration for sequential version: $anotherDuration")

        // val example = reads(0)
        // println("Creating a suffix array for example" + example)

        // val startSA = System.currentTimeMillis()
        // // println(example)        
        // val sufArr: Dataset[Row] = SuffixArrayBuilder.createFullSuffixArray(example, verbose = false)
        // val durationSA = System.currentTimeMillis() - startSA
        // println(f"Created suffix array in $durationSA ms")
        
        // println(sufArr.getClass())


        Console.exiting()
		SparkController.destroy()
	}

}



        // var first = "ACCA"
        // var second = "CCACC"
        // println(f"First seqeuence: " + first)
        // println(f"Second sequence: " + second)

        // val scores: Array[Array[Integer]] = Array.ofDim[Integer](2, 2)
        // scores(0)(0) = 2
        // scores(1)(1) = 2
        // scores(0)(1) = -1 
        // scores(1)(0) = -1

        // AlignSearcher.displayScoreMatrix(scores)
        // AlignSearcher.smithWatermanSearch(Array(first, second), scores, penalty = -3)