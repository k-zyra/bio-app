package examples

/* External imports */
import scala.collection.mutable.{ArrayBuffer, ArrayBuilder}
import scala.collection.parallel.mutable.ParArray

/* Internal imports */
import app.SparkController
import bio.searchers.AlignSearcher
import misc.{Console, Constants}
import utils.{FileUtils, KmerUtils}



object LocalAlignmentExample {
    def getAllAlignments(firstSequence: String,
                        sequences: Array[String],
                        verbose: Boolean = false): Unit = {
        val substitutionMatrix: Array[Array[Int]]= AlignSearcher.prepareSubstitutionMatrix()
        val alignments: ArrayBuilder.ofRef[(String, String)] = new ArrayBuilder.ofRef[(String, String)]()

        for (secondSequence <- sequences) {
            if (firstSequence != secondSequence) {
                var matches = AlignSearcher.smithWatermanAlignment(Array(firstSequence, secondSequence),
                                                                substitutionMatrix, verbose = false)
                alignments ++= matches
            } 
        }

        if (verbose) println(f"Number of alignments generated: ${alignments.result().length}")
    }


    def runSequential(sequences: Array[String]): Unit = {
        val start: Long = System.nanoTime()
        for (sequence <- sequences) {
            this.getAllAlignments(sequence, sequences)
        }
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis
        
        println(f"Time spent in sequential LocalAlignmentExample: ${duration} ms")
    }


    def runParallel(sequences: Array[String]): Unit = {
        val sequencesPar: ParArray[String] = sequences.par

        val start: Long = System.nanoTime()
        sequencesPar.map(sequence => this.getAllAlignments(sequence, sequences))
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        println(f"Time spent in parallel LocalAlignmentExample: ${duration} ms")
    }


    def runSingle(): Unit = {
        val firstSequence: String = "ACCA"
        val secondSequence: String = "CCACC"
        val substitutionMatrix: Array[Array[Int]] =
                                AlignSearcher.prepareSubstitutionMatrix("substitutionMatrix_local.xml")
        val alignments: Array[(String, String)] =
                                AlignSearcher.smithWatermanAlignment(Array(firstSequence, secondSequence), substitutionMatrix)

        println(f"Alignments for sequences: ${firstSequence} and ${secondSequence}: ${substitutionMatrix.length}")
        for (pair <- alignments) AlignSearcher.displayAlignments(pair)
    }


    def main(args: Array[String]): Unit = {
        val session = SparkController.getSession()
        val context = SparkController.getContext()

        // ======================================

        this.runSingle()

        // ======================================

		val fastqFile = "C:\\Users\\karzyr\\Desktop\\pacbio_short.fastq"
		val fastqContent = FileUtils.readFile(fastqFile)
        // val rddFile = context.textFile(fastqFile)
        // println(rddFile.count())
        val reads = fastqContent.getReads()
        val kmers = KmerUtils.prepareAllKmers(reads.slice(0, 10), k=13, verbose = true)
        println(f"Number of generated kmers: ${kmers.length}")

        val kmerSubset = KmerUtils.getKmers(kmers.slice(0,100))
        val seqAlignments = this.runSequential(kmerSubset)
        val parAlignments = this.runParallel(kmerSubset)

        // ======================================

        Console.exiting()
        SparkController.destroy()
    }
}
