package app

/* External imports */
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.SparkContext._
import org.apache.spark.sql.SparkSession

/* Internal imports */
import misc.Logger



object SparkController {
    private val session: SparkSession = SparkSession.builder
            .appName("BIO-APP")
//             .master("local[*]")                                  // Locally, max. number of cores
            // .master("spark://192.168.1.32:7077")                 // Ubuntu
            // .master("spark://192.168.1.101:7077")           // Windows
            .master("spark://192.168.1.31:7077")
            .config("spark.log.level", "WARN")
            .getOrCreate()
    private val context = this.session.sparkContext


    /** Stop Spark session
     *  Before shutting down, to discard stack traces, log level is set to WARN 
     */
    def destroy(verbose: Boolean = false): Unit = {
        if (!verbose) this.context.setLogLevel("FATAL")
        this.session.stop()
    }


    /** Get Spark Session object
     */
    def getSession(): SparkSession = {
        return this.session
    }


    /** Get Spark Context object
     */
    def getContext(): SparkContext = {
        return this.context
    }


    /** Set log level
    *   Define log level for SparkContext
     */
    def setLogLevel(level: String) : Unit = {
        context.setLogLevel(level)
        println(s"Set logging level to $level")
    }

}
