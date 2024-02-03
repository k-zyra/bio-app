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

		val fastqFile = "C:\\Users\\karzyr\\Desktop\\pacbio.fastq"
		FileUtils.statistics(fastqFile)

		val fastqContent = FileUtils.readFile(fastqFile)
        val reads = fastqContent.getReads()
		val readsAndScores = fastqContent.getReadsAndScores()

        Console.exiting()
		SparkController.destroy()
	}

}