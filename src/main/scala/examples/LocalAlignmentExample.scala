package examples

/* External imports */
import scala.collection.mutable.ArrayBuffer
import scala.collection.parallel.mutable.ParArray

/* Internal imports */
import app.SparkController

import bio.searchers.AlignSearcher

import utils.Console
import utils.Constants
import utils.FileUtils
import utils.KmerUtils



object LocalAlignmentExample {
    def getAllAlignments(firstSequence: String,
                        sequences: Array[String]): Unit = {
        val substitutionMatrix: Array[Array[Int] ]= AlignSearcher.prepareSubstitutionMatrix()

        for (secondSequence <- sequences) {
            if (firstSequence != secondSequence) {
                var matches = AlignSearcher.smithWatermanAlignment(Array(firstSequence, secondSequence),
                                                                substitutionMatrix, verbose = false)
            } 
        }
    }


    def runSequential(sequences: Array[String]): Unit = {
        var buffer: ArrayBuffer[Integer] = new ArrayBuffer[Integer]

        val start: Long = System.nanoTime()
        for (sequence <- sequences) {
            this.getAllAlignments(sequence, sequences)
        }
        val duration: Float = (System.nanoTime() - start)/Constants.NANO_IN_MILLIS
        
        println("Time spent in sequential LocalAlignmentExample: " + duration + " ms")
    }


    def runParallel(sequences: Array[String]): Unit = {
        val sequencesPar: ParArray[String] = sequences.par

        val start: Long = System.nanoTime()
        sequencesPar.map(sequence => this.getAllAlignments(sequence, sequences))
        val duration: Float = (System.nanoTime() - start)/Constants.NANO_IN_MILLIS

        println("Time spent in parallel LocalAlignmentExample: " + duration + " ms")
    }


    def main(args: Array[String]): Unit = {
        // var arguments = utils.OptionParser.parseArguments(args)

        val session = SparkController.getSession()
        val context = SparkController.getContext()

		val fastqFile = "C:\\Users\\karzyr\\Desktop\\pacbio.fastq"
		FileUtils.statistics(fastqFile)

		val fastqContent = FileUtils.readFile(fastqFile)
        val reads = fastqContent.getReads()
        val kmers = KmerUtils.prepareAllKmers(reads.slice(0, 10), k=13, verbose = true)
        println(f"Number of generated kmers: ${kmers.length}")

        val kmerSubset = KmerUtils.getKmers(kmers.slice(0,1000))
        val seqAlignments = this.runSequential(kmerSubset)
        val parAlignments = this.runParallel(kmerSubset)

        // ======================================

        Console.exiting()
        SparkController.destroy(verbose = true)
    }
}
