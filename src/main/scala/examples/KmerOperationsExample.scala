package examples

/* Internal imports */
import app.SparkController
import misc.Console
import utils.{FileUtils, KmerUtils}


object KmerOperationsExample {
    private var verbose: Boolean = false

    def runSequential(reads: Array[String],
                    subsetSize: Int = 50): Unit = {
		val allkmersSeq = KmerUtils.prepareAllKmersSequential(reads.slice(0, subsetSize), k=13, verbose = true)
		 println(f"Number of kmers (sequential): ${allkmersSeq.length}")
    }   


    def runParallel(reads: Array[String],
                    subsetSize: Int = 50): Unit = {
		val allkmersPar = KmerUtils.prepareAllKmers(reads.slice(0, subsetSize), k=13, verbose = true)
		 println(f"Number of kmers (parallel): ${allkmersPar.length}")
    }


    def runSingle(): Unit = {
        val sequence: String = "SUCCESS"
        val kmers: Seq[String] = KmerUtils.generateKmers(sequence, k=3, verbose = true)

        println(f"Kmers generated from sequence: ${sequence}")
        for (kmer <- kmers) println(kmer)
    }


	def main(args: Array[String]): Unit = {
        val fastqFile = "C:\\Users\\karzyr\\Desktop\\pacbio.fastq"
        val reads = FileUtils.getReadsFromFile(fastqFile)

        if (verbose) this.runSingle()
        this.runSequential(reads)
        this.runParallel(reads)

        Console.exiting()
        SparkController.destroy(verbose)
    }
}
