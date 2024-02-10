package examples

/* External imports */
import scala.collection.mutable.ArrayBuffer
import scala.collection.parallel.mutable.ParArray

/* Internal imports */
import app.SparkController
import bio.searchers.AlignSearcher
import utils.{Console, Constants, FileUtils, KmerUtils}



object GlobalAlignmentExample {
    def getAllAlignments(firstSequence: String,
                        sequences: Array[String]): Unit = {
        val substitutionMatrix: Array[Array[Int] ]= AlignSearcher.prepareSubstitutionMatrix()

        for (secondSequence <- sequences) {
            if (firstSequence != secondSequence) {
                AlignSearcher.needlemanWunschAlignment(Array(firstSequence, secondSequence),
                                                            substitutionMatrix, verbose = false)
            } 
        }
    }


    def runSequential(sequences: Array[String]): Unit = {
        val start: Long = System.nanoTime()
        for (sequence <- sequences) {
            this.getAllAlignments(sequence, sequences)
        }
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis
        
        println(f"Time spent in sequential GlobalAlignmentExample: ${duration} ms")
    }


    def runParallel(sequences: Array[String]): Unit = {
        val sequencesPar: ParArray[String] = sequences.par

        val start: Long = System.nanoTime()
        var alignments = sequencesPar.map(sequence => this.getAllAlignments(sequence, sequences))
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        println(f"Time spent in parallel GlobalAlignmentExample: ${duration} ms")
    }


    def runSingle(): Unit = {
        val firstSequence: String = "GATTA"
        val secondSequence: String = "GAATTC"
        val substitutionMatrix: Array[Array[Int]] =
                                AlignSearcher.prepareSubstitutionMatrix("substitutionMatrix_global.xml")
                                AlignSearcher.displaySubstitutionMatrix(substitutionMatrix)
        val alignments: Array[(String, String)] =
                                AlignSearcher.needlemanWunschAlignment(Array(firstSequence, secondSequence), substitutionMatrix,
                                5, -5, -5)

        println(f"Alignments for sequences: ${firstSequence} and ${secondSequence}: ${substitutionMatrix.length}")
        for (pair <- alignments) AlignSearcher.displayAlignments(pair)
    }


    def main(args: Array[String]): Unit = {
        val session = SparkController.getSession()
        val context = SparkController.getContext()

        // ======================================

        this.runSingle()

        // ======================================

		val fastqFile = "C:\\Users\\karzyr\\Desktop\\pacbio.fastq"
		val fastqContent = FileUtils.readFile(fastqFile)
        val reads = fastqContent.getReads()
        val kmers = KmerUtils.prepareAllKmers(reads.slice(0, 10), k=13, verbose = true)
        println(f"Number of generated kmers: ${kmers.length}")

        val kmerSubset = KmerUtils.getKmers(kmers.slice(0,100))
        this.runSequential(kmerSubset)
        this.runParallel(kmerSubset)

        // ======================================

        Console.exiting()
        SparkController.destroy(verbose = true)
    }
}
