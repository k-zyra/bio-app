package examples

/* Internal imports */
import app.SparkController
import misc.Console
import utils.FileUtils
import utils.ScoreUtils



object FilteringExample {
    def runSequential(readsAndScores: Array[(String, String)]): Unit = {
        println("Filtering by mean quality of read")
		ScoreUtils.filterByMeanQualitySequential(readsAndScores, 30, 33, verbose = true)

        println("Filtering by number of base calls with satisfying quality")
        ScoreUtils.filterByNumberOfCorrectBaseCallSequential(readsAndScores, 30, 50, 33, verbose = true)
    }   


    def runParallel(readsAndScores: Array[(String, String)]): Unit = {
        println("Filtering by mean quality of read")
		ScoreUtils.filterByMeanQualityParallel(readsAndScores, 30, 33, verbose = true)

        println("Filtering by number of base calls with satisfying quality")
        ScoreUtils.filterByNumberOfCorrectBaseCallParallel(readsAndScores, 30, 50, 33, verbose = true)
    }


	def main(args: Array[String]): Unit = {
        val session = SparkController.getSession()
        val context = SparkController.getContext()

        val fastqFile = "C:\\Users\\karzyr\\Desktop\\pacbio.fastq"
        val fastqContent = FileUtils.readFile(fastqFile)
        val readsAndScores = fastqContent.getReadsAndScores()

        this.runSequential(readsAndScores)
        this.runParallel(readsAndScores)

        Console.exiting()
        SparkController.destroy(verbose = true)
    }
}
