package examples

/* External imports */
import scala.collection.mutable.ArrayBuffer
import scala.collection.parallel.mutable.ParArray

/* Internal imports */
import app.SparkController

import bio.searchers.AlignSearcher

import utils.Console
import utils.FileUtils
import utils.KmerUtils



object LocalAlignmentExample {
    def getAllAlignments(firstSequence: String,
                        sequences: Array[String]): Integer = {
        val scoreMatrix: Array[Array[Int] ]= AlignSearcher.prepareScoreMatrix()
        var numberOfAlignments: Integer = 0

        for (secondSequence <- sequences) {
            if (firstSequence != secondSequence) {
                var matches = AlignSearcher.smithWatermanSearch(Array(firstSequence, secondSequence),
                                                                scoreMatrix, verbose = false)
                numberOfAlignments += matches
            } 
        }

        return numberOfAlignments
    }


    def runSequential(sequences: Array[String]): Array[Integer] = {
        var buffer: ArrayBuffer[Integer] = new ArrayBuffer[Integer]

        val start: Float = System.currentTimeMillis()
        for (sequence <- sequences) {
            val alignments: Integer = this.getAllAlignments(sequence, sequences)
            buffer += alignments
        }
        val duration: Float = System.currentTimeMillis() - start
        
        println("Time spent in sequential LocalAlignmentExample: " + duration + " ms")
        return buffer.toArray
    }


    def runParallel(sequences: Array[String]): Array[Integer] = {
        val sequencesPar: ParArray[String] = sequences.par

        val start: Float = System.currentTimeMillis()
        val alignemnts = sequencesPar.map(sequence => this.getAllAlignments(sequence, sequences))
        val duration: Float = System.currentTimeMillis() - start

        println("Time spent in parallel LocalAlignmentExample: " + duration + " ms")
        return alignemnts.toArray
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
