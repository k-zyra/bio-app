package examples

/* Internal imports */
import app.SparkController
import bio.utils.{FileUtils, ScoreUtils}
import misc.Console


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
        val fastqFile = "C:\\Users\\karzyr\\Desktop\\pacbio.fastq"
        val readsAndScores = FileUtils.getReadsAndScoresFromFile(fastqFile)

        this.runSequential(readsAndScores)
        this.runParallel(readsAndScores)

        Console.exiting()
        SparkController.destroy()
    }
}
