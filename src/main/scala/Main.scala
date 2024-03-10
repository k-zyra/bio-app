package app

/* External imports */

/* Internal imports */ 
import misc.{Console, Constants}
import utils._

import bio.align.multiple.GeneticAlgorithm
import bio.align.multiple.Fitness



object BioApp {
	def main(args: Array[String]): Unit = {
		val session = SparkController.getSession()

		Console.exiting()
		SparkController.destroy()
	}

}