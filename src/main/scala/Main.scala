package app

/* External imports */ 
import java.nio.file.Paths

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

import utils._
import bio.clustering.BasicCluster
import bio.clustering.KmeansCluster
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext.Basic


object BioApplication extends App {
	def main: Unit = 
		println("Hello")

		val fastqFile = "C:\\Users\\karzyr\\Desktop\\pacbio.fastq"
		FileUtils.statistics(fastqFile)

		val fastqContent = FileUtils.readFile(fastqFile)
		println(fastqContent)

		val reads = fastqContent.getReads()

		val allkmerspar = KmerCounter.prepareAllKmers(reads.slice(0, 50), k=13, verbose = true)
		val allkmers = KmerCounter.prepareAllKmers(reads.slice(0,50), k=13, verbose = true)
		println("Number of kmers: " + allkmers.length)

		var exampleKmer = allkmers(10)
		println(s"Example kmer: $exampleKmer")
		var candidates = StringUtils.findOverlapCandidates(exampleKmer._1, KmerCounter.getKmers(allkmers), overlapLength=5, verbose=true)

		var candidate = candidates(10)
		println(s"Choosen candidate: $candidate")

		KmeansCluster.createClusters(KmerCounter.getKmers(allkmers))
		// KmeansCluster.showClusters(rows = 40, filter = Some(1))

		var clusters = KmeansCluster.getClusterAsArray()
		println("Without filtering: " + clusters.length)

		var filteredClusters = KmeansCluster.getClusterAsArray(prediction = Some(2))
		println("With filtering: " + filteredClusters.length)
		println("filtered cluster type: " + filteredClusters.getClass() + ", should be array")
		// filteredClusters.foreach(println)

		// for (element <- filteredClusters) {
		// 	println(element.toString())
		// }
		var clusterId = KmeansCluster.findCluster("ACCATGCGGCCTT")
		println(clusterId)



		BasicCluster.createClusters(KmerCounter.getKmers(allkmers), clusters = 6)


		SparkController.destroy()
	}

