package examples

/* External imports */
import scala.io.StdIn

/* Internal imports */
import app.SparkController

import utils.Console
import utils.FileUtils
import utils.ScoreCalculator



object FilteringExample {
    def runSequential(readsAndScores: Array[(String, String)]): Unit = {
		ScoreCalculator.filterByMeanQualitySequential(readsAndScores, 30, 33, verbose = true)
    }   


    def runParallel(readsAndScores: Array[(String, String)]): Unit = {
		ScoreCalculator.filterByMeanQualityParallel(readsAndScores, 30, 33, verbose = true)
    }


	def main(args: Array[String]): Unit = {
        // var arguments = utils.OptionParser.parseArguments(args)
        val session = SparkController.getSession()
        val context = SparkController.getContext()

		val fastqFile = "C:\\Users\\karzyr\\Desktop\\pacbio.fastq"
		FileUtils.statistics(fastqFile)

		val fastqContent = FileUtils.readFile(fastqFile)
        val readsAndScores = fastqContent.getReadsAndScores()

        this.runSequential(readsAndScores)
        this.runParallel(readsAndScores)

        Console.exiting()
        SparkController.destroy(verbose = true)
    }
}
