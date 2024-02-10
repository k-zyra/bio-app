package examples

/* Internal imports */
import app.SparkController
import utils.{Console, Constants, FileUtils, KmerUtils, StringUtils}



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


	def runSingle(): Unit = {
		val sequence: String = "MISSISSIPPI"
		val bwt = StringUtils.burrowsWheelerTransform(sequence, verbose = true)

		println(f"Burrows-Wheeler transform generated from sequence: ${sequence}")
		println(bwt)
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
		val kmersWithCounters = KmerUtils.prepareAllKmers(reads.slice(0, 10), k=13, verbose = true)
        println(f"Number of generated kmers: ${kmersWithCounters.length}")

		val kmers: Array[String] = KmerUtils.getKmers(kmersWithCounters)
		this.runSequential(kmers)
		this.runParallel(kmers)
	
        // ======================================

		Console.exiting()
        SparkController.destroy(verbose = true)
	}
}
