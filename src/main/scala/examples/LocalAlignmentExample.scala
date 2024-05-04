package examples

/* External imports */
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.collection.parallel.mutable.ParArray

/* Internal imports */
import app.SparkController
import bio.searchers.AlignSearcher
import bio.utils.{FileUtils, KmerUtils}
import misc.{Console, Constants}



object LocalAlignmentExample {
    private var verbose: Boolean = false

    def getAllAlignments(firstSequence: String,
                        sequences: Array[String],
                        verbose: Boolean = false): Array[(String, String)] = {
        val substitutionMatrix: Array[Array[Int]]= AlignSearcher.prepareSubstitutionMatrix(Constants.LocaLDefaultMatrix)
        val alignments: mutable.ArrayBuilder.ofRef[(String, String)] = new mutable.ArrayBuilder.ofRef[(String, String)]()

        val start: Long = System.nanoTime()
        for (secondSequence <- sequences) {
            if (firstSequence != secondSequence) {
                val matches = AlignSearcher.smithWatermanAlignment(Array(firstSequence, secondSequence),
                                                                substitutionMatrix, verbose = false)
                alignments ++= matches
            }
        }
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) println(f"Time spent in GlobalAlignmentExample: ${duration} ms")
        alignments.result()
    }


    def runSequential(sequences: Array[String]): Array[(String, String)] = {
        val alignments: ArrayBuffer[(String, String)] = new ArrayBuffer[(String, String)]()
        val start: Long = System.nanoTime()
        for (sequence <- sequences) {
            alignments ++= this.getAllAlignments(sequence, sequences)
        }
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis
        
        println(f"Time spent in sequential LocalAlignmentExample: ${duration} ms")
        alignments.toArray
    }


    def runParallel(sequences: Array[String]): Unit = {
        val sequencesPar: ParArray[String] = sequences.par

        val start: Long = System.nanoTime()
        sequencesPar.map(sequence => this.getAllAlignments(sequence, sequences))
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        println(f"Time spent in parallel LocalAlignmentExample: ${duration} ms")
    }


    def runSingle(): Array[(String, String)] = {
        val firstSequence: String = "ACCA"
        val secondSequence: String = "CCACC"
        val substitutionMatrix: Array[Array[Int]] =
                                AlignSearcher.prepareSubstitutionMatrix("substitutionMatrix_local.xml")
        val alignments: Array[(String, String)] =
                                AlignSearcher.smithWatermanAlignment(Array(firstSequence, secondSequence), substitutionMatrix)

        println(f"Alignments for sequences: ${firstSequence} and ${secondSequence}: ${substitutionMatrix.length}")
        for (pair <- alignments) AlignSearcher.displayAlignments(pair)

        alignments
    }


    def main(args: Array[String]): Unit = {
        val fastqFile = "C:\\Users\\karzyr\\Desktop\\pacbio_short.fastq"
        val reads: Array[String] = FileUtils.getReadsFromFile(fastqFile)
        val kmersWithCounters = KmerUtils.prepareAllKmers(reads.slice(0, 10), k=13, verbose = true)
        val kmers = KmerUtils.getKmers(kmersWithCounters.slice(0,100))

        this.runSingle()
        this.runSequential(kmers)
        this.runParallel(kmers)

        Console.exiting()
        SparkController.destroy(verbose)
    }
}
