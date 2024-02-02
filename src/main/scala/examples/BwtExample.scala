package examples

/* Internal imports */
import app.SparkController

import utils.Console
import utils.Constants
import utils.FileUtils
import utils.KmerUtils
import utils.StringUtils



object BwtExample {
    def runSequential(sequences: Array[String]): Unit = {
		val start: Long = System.nanoTime()
		for (sequence <- sequences) {
			StringUtils.burrowsWheelerTransform(sequence, verbose = false)
		}
		val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

		println(f"Time spent in sequential BwtExample: ${duration} ms")
    }


	def runParallel(sequences: Array[String]): Unit = {
		val sequenesPar = sequences.par

		val start: Long = System.nanoTime()
		val bwtsPar = sequenesPar.map(sequence => StringUtils.burrowsWheelerTransform(sequence, verbose = false))
		val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

		println(f"Time spent in parallel BwtExample: ${duration} ms")
    }


	def main(args: Array[String]): Unit = {
		// var arguments = utils.OptionParser.parseArguments(args)
        
        val session = SparkController.getSession()
        val context = SparkController.getContext()

		val fastqFile = "C:\\Users\\karzyr\\Desktop\\pacbio.fastq"
		FileUtils.statistics(fastqFile)

		val fastqContent = FileUtils.readFile(fastqFile)
        val reads = fastqContent.getReads()
		val kmersWithCounters = KmerUtils.prepareAllKmers(reads.slice(0, 10), k=13, verbose = true)
        println(f"Number of generated kmers: ${kmersWithCounters.length}")

		val kmers: Array[String] = KmerUtils.getKmers(kmersWithCounters)
		this.runSequential(kmers)
		this.runParallel(kmers)
	
		Console.exiting()
        SparkController.destroy(verbose = true)
	}
}
