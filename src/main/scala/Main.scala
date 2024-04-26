package app

/* External imports */

/* Internal imports */

import benchmarks.{GlobalAlignmentBenchmark, LocalAlignmentBenchmark, MsaBenchmark}
import misc.{Console, Constants}
import bio.align.multiple.{BlockMutation, Config, Crossover, Fitness, GapMutation, GeneticAlgorithm, Mutation, Utils}
import bio.searchers.AlignSearcher
import bio.utils.FileUtils
import types.Biotype.CurrentPopulation

import scala.collection.mutable.ArrayBuffer


object BioApp {
	def main(args: Array[String]): Unit = {
		val session = SparkController.getSession()

		val balibase = "C:\\Users\\karzyr\\Desktop\\BB11001.tfa"
		val tfaFile = FileUtils.readFile(balibase)
		val tfaReads: Array[String] = tfaFile.getReads()


		Mutation.setupMutationSettings()

//		val fastqFile = "C:\\Users\\karzyr\\Desktop\\pacbio_short.fastq"
//		val reads: Array[String] = FileUtils.getReadsFromFile(fastqFile)
//		val kmersWithCounters = KmerUtils.prepareAllKmers(reads.slice(0, 10), k=13, verbose = true)
//		val kmers = KmerUtils.getKmers(kmersWithCounters.slice(0,100))
//
//		val first= "CATGCATCTTTAC"
//		val second="ACCCACTCGCTTG"


		val short=Array("BBCACBAB----------------", "--C-CAAABA--------C-----", "--C-CAABBAB-------------", "CBCACBAB----------------")
		Config.initialAverageLength = 24

		println(s"Score before: ${Fitness.getAlignmentCost(short)}")

		println("Move reference seqeunce")
		val shifted = BlockMutation.moveReferenceSequence(short)
		println("Shifted mutant")
		shifted.foreach(println)
		println(s"SCore after: ${Fitness.getAlignmentCost(shifted)}")


		BlockMutation.localRealignment(short)

		//
//		val exampleWithGaps = Array("AAXW--WR--WE-W-WWW--------------------")
//		println("Before")
//		exampleWithGaps.foreach(println)
//
//		val mutant = GapMutation.extendGap(exampleWithGaps)
//		println("After")
//		mutant.foreach(println)

//		for (i <- 0 to 10) {
//			val mutant = GapMutation.insertSingleGap(Array(first, second))
////			val mutant = GapMutation.insertGap(Array(first, second))
////			println(s"mutant: ${mutant}")
//			println(s"Mutant: ${i}")
//			mutant.foreach(println)
//			println()
//		}


//		val sequences = MsaExample.shortSequences

		// Example usage
//		val strings = Array("apple", "apricot", "avocado")

//		val firstAl = Array("CCCCCCCCCC", "AACAAAACAA")
//		println("First al")
//		firstAl.foreach(println)
//		println(s"First score: ${Fitness.getAlignmentCost(firstAl)}")


//		val secondAl = Array("BBCBBBBCBB", "AACBBDDCBB")
//		println("Seoncd al")
//		secondAl.foreach(println)
//		println(s"Second score: ${Fitness.getAlignmentCost(secondAl)}")

//		val newChild = Crossover.uniform(firstAl, secondAl, verbose = true)
//		println("New child:")
//		newChild.foreach(println)
//		println(s"Child score: ${Fitness.getAlignmentCost(newChild)}")


//		val local = LocalAlignmentExample.getAllAlignments(first, Array(second), verbose = true)
//		println("All local alignments")
//		local.foreach(AlignSearcher.displayAlignments)
////		local.foreach(println)
//		println()

//		val global = GlobalAlignmentExample.getAllAlignments(first, Array(second), verbose = true)
//		println("All global alignments")
//		global.foreach(AlignSearcher.displayAlignments)

//		val sample = Array(first, second)
//		Config.set(_maxNumberOfEpochs = 50)
//		GeneticAlgorithm.prepareInitialPoint(sample)
//		Mutation.setupMutationSettings()
//		val genetic = GeneticAlgorithm.start(sample)
//		genetic.head.foreach(println)

		Console.exiting()
		SparkController.destroy()
	}

}