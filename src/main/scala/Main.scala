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


object BioApp {
	def main(args: Array[String]): Unit = {
		// var arguments = utils.OptionParser.parseArguments(args)

        val session = SparkController.getSession()
        val context = SparkController.getContext()

		val fastqFile = "C:\\Users\\karzyr\\Desktop\\pacbio.fastq"
		FileUtils.statistics(fastqFile)

		val fastqContent = FileUtils.readFile(fastqFile)
		println(fastqContent)

        val sequences = fastqContent.getSequences()
        val reads = fastqContent.getReads()

        for (n <- 0 to 10) println(sequences(0))
        println("Number of reads: " + fastqContent.getNumberOfReads())

		val allkmersSeq = KmerUtils.prepareAllKmersSequential(reads.slice(0, 50), k=13, verbose = true)
		val allkmersPar = KmerUtils.prepareAllKmers(reads.slice(0, 50), k=13, verbose = true)

		println("Number of kmers (sequential): " + allkmersSeq.length)
		println("Number of kmers (parallel): " + allkmersPar.length)


        val realParKmers = allkmersPar.par
        val start = System.currentTimeMillis()
        val allSufsPar = realParKmers.map(kmer => SuffixArrayBuilder.generateSuffixes(kmer._1, verbose = false))
        val duration = System.currentTimeMillis()  - start
        println(f"Duration for parallel version: $duration")

        val anotherStart = System.currentTimeMillis()
        for (kmer <- allkmersPar) {
            SuffixArrayBuilder.generateSuffixes(kmer._1, verbose = false)
        }
        val anotherDuration = System.currentTimeMillis()  - anotherStart
        println(f"Duration for sequential version: $anotherDuration")

        val example = allkmersPar(0)._1
        println("Creating a suffix array for example" + example)
        val startSA = System.currentTimeMillis()
        SuffixArrayBuilder.createFullSuffixArray(example, verbose = false)
        val durationSA = System.currentTimeMillis() - startSA
        println(f"Created suffix array in $durationSA")



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