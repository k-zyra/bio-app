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


object BioApplication extends App {
	def main: Unit = {
		var arguments = utils.OptionParser.parseArguments(args)

        var first = "ACCA"
        var second = "CCACC"

        val scores: Array[Array[Integer]] = Array.ofDim[Integer](2, 2)
        scores(0)(0) = 2
        scores(1)(1) = 2
        scores(0)(1) = -1 
        scores(1)(0) = -1

        AlignSearcher.displayScoreMatrix(scores)
        AlignSearcher.smithWatermanSearch(Array(first, second), scores, penalty = -3)

		SparkController.destroy()
	}

}
