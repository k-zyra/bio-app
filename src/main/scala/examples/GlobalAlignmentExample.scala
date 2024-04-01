package examples

/* External imports */
import scala.collection.mutable
import scala.collection.parallel.mutable.ParArray

/* Internal imports */
import app.SparkController
import bio.searchers.AlignSearcher
import misc.{Console, Constants}
import utils.{FileUtils, KmerUtils}



object GlobalAlignmentExample {
    private var verbose: Boolean = false

    def getAllAlignments(firstSequence: String,
                         sequences: Array[String],
                         verbose: Boolean = false): Array[(String, String)] = {
        val substitutionMatrix: Array[Array[Int] ]= AlignSearcher.prepareSubstitutionMatrix(Constants.GlobalDefaultMatrix)
        val alignments: mutable.ArrayBuilder.ofRef[(String, String)] = new mutable.ArrayBuilder.ofRef[(String, String)]()

        val start: Long = System.nanoTime()
        for (secondSequence <- sequences) {
            if (firstSequence != secondSequence) {
                alignments ++= AlignSearcher.needlemanWunschAlignment(Array(firstSequence, secondSequence),
                                                            substitutionMatrix, verbose = false)
            } 
        }
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) println(f"Time spent in GlobalAlignmentExample: ${duration} ms")
        return alignments.result()
    }


    def runSequential(sequences: Array[String]): Array[(String, String)] = {
        val alignments: mutable.ArrayBuilder.ofRef[(String, String)] = new mutable.ArrayBuilder.ofRef[(String, String)]()
        val start: Long = System.nanoTime()
        for (sequence <- sequences) {
            alignments ++= this.getAllAlignments(sequence, sequences)
        }
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis
        
        println(f"Time spent in sequential GlobalAlignmentExample: ${duration} ms")
        return alignments.result()
    }


    def runParallel(sequences: Array[String]): Array[(String, String)] = {
        val sequencesPar: ParArray[String] = sequences.par

        val start: Long = System.nanoTime()
        var alignments = sequencesPar.map(sequence => this.getAllAlignments(sequence, sequences))
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        println(f"Time spent in parallel GlobalAlignmentExample: ${duration} ms")
        return alignments.flatten.toArray
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
        val fastqFile: String = "C:\\Users\\karzyr\\Desktop\\pacbio.fastq"
		val reads: Array[String] = FileUtils.getReadsFromFile(fastqFile)
        val kmersWithCounters = KmerUtils.prepareAllKmers(reads.slice(0,10), k=13, verbose = true)
        val kmers = KmerUtils.getKmers(kmersWithCounters.slice(0,100))

        if (verbose) this.runSingle()
        this.runSequential(kmers)
        this.runParallel(kmers)

        // ======================================

        Console.exiting()
        SparkController.destroy(verbose)
    }
}
