package pipeline

/* External imports */
import edu.umd.marbl.mhap.main.MhapMain

/* Internal imports */
import misc.{Constants, Logger}
import utils.MetricsUtils



object CorrectionPhase {
    private val logger = new Logger("CorrectionPhase")

    private var isMhapConfigured: Boolean = false


    /** Prepare parameters for MHAP
     *  MHAP sensitivity mode and proper parameter values are chosen based on estimated genome coverage.
     */
    private def configureMhap(filename: String = Constants.EmptyString,
                              numberOfThreads: Int = 1,
                              verbose: Boolean = logger.isVerbose()): Array[String] = {
        logger.logInfo("Configuring MHAP...")

        if (!(filename.endsWith(Constants.FastaExtension))){
            logger.logWarn("Cannot prepare MHAP for processing FASTQ file")
            return Constants.EmptyStringArray
        }

        // val coverage: Float = MetricsUtils.estimateTotalCoverage(10)
        val coverage = 24.14
        if (coverage <= 0) {
            logger.logWarn("Cannot prepare MHAP to work with 0% coverage.")
            return Constants.EmptyStringArray
        }

        // Choose number of threads
        var numThreads: String = numberOfThreads.toString

        // Prepare parameter variables
        var mhapMode: String = Constants.EmptyString
        var numHashes: String = Constants.EmptyString
        var minNumMatches: String = Constants.EmptyString
        var threshold: String = Constants.EmptyString
        var ordSketch: String = Constants.EmptyString
        var ordSketcgerMer: String = Constants.EmptyString

        // Get values for parameters
        if (coverage <= 30) {
            mhapMode = "high"
            numHashes = "768"
            minNumMatches = "2"
            threshold = "0.73"
            ordSketch = "1536"
//            $ordSketchMer = getGlobal("${tag}MhapOrderedMerSize");
        } else if (coverage < 60) {
            mhapMode = "normal"
            numHashes = "512"
            minNumMatches = "3"
            threshold = "0.78"
            ordSketch = "1000"
//            $ordSketchMer = getGlobal("${tag}MhapOrderedMerSize") + 2;
        } else {
            mhapMode = "low"
            numHashes = "256"
            minNumMatches = "3"
            threshold = "0.80"
            ordSketch = "1000"
//            ordSketchMer = getGlobal("${tag}MhapOrderedMerSize") + 2;
        }

        var parameters: Array[String] = Array(
            "-s", filename,
            "--num-hashes", numHashes,
            "--num-threads", numThreads,
        )
        this.isMhapConfigured = true

        if (verbose) {
            logger.logInfo("Parameters for MHAP prepared.")
            logger.logInfo("Done.")
        }
        return parameters
    }


    /** Start MHAP alignment tool
     *  MHAP is used for efficient alignment searching.
     */
    def runMhap(verbose: Boolean = logger.isVerbose()): Unit = {
        val parameters: Array[String] = this.configureMhap()

        MhapMain.main(parameters)
    }


    /** Start correction phase.
     * 
     *  This phase consists of the following steps:
     *      1. Read file 
     *      2. Count bases
     *      3. Generate kmers
     *      4. Filter kmers
     *      5. Estimate genome coverage
     *      6. Start MHAP tool
     *      7. 
     */
    def start(): Unit = {
        logger.logInfo("CORRECTION PHASE has started.")
    }
}