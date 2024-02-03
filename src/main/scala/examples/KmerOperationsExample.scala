package examples

/* External imports */
import scala.io.StdIn

/* Internal imports */
import app.SparkController

import utils.Console
import utils.FileUtils
import utils.KmerUtils



object KmerOperationsExample {
    def runSequential(reads: Array[String],
                    subsetSize: Int = 50): Unit = {
		val allkmersSeq = KmerUtils.prepareAllKmersSequential(reads.slice(0, subsetSize), k=13, verbose = true)
		println("Number of kmers (sequential): " + allkmersSeq.length)
    }   


    def runParallel(reads: Array[String],
                    subsetSize: Int = 50): Unit = {
		val allkmersPar = KmerUtils.prepareAllKmers(reads.slice(0, subsetSize), k=13, verbose = true)
		println("Number of kmers (parallel): " + allkmersPar.length)
    }


	def main(args: Array[String]): Unit = {
        // var arguments = utils.OptionParser.parseArguments(args)

        val session = SparkController.getSession()
        val context = SparkController.getContext()

		val fastqFile = "C:\\Users\\karzyr\\Desktop\\pacbio.fastq"
		FileUtils.statistics(fastqFile)

		val fastqContent = FileUtils.readFile(fastqFile)
        val reads = fastqContent.getReads()
        println("Number of reads: " + fastqContent.getNumberOfReads())

        this.runSequential(reads)
        this.runParallel(reads)

        Console.exiting()
        SparkController.destroy(verbose = true)
    }
}
