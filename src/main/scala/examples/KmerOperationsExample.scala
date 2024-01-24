package examples

/* External imports */
import scala.io.StdIn

/* Internal imports */
import app.SparkController

import utils.KmerUtils
import utils.Console


object KmerOperationsExample {
	def main(args: Array[String]): Unit = {
        
        val session = SparkController.getSession()
        val context = SparkController.getContext()

        println("Hello from KmerOperationsExample")

        Console.exiting()        
        SparkController.destroy()
    }

    // def runSequential(): Unit = {

    // }

    // def runParallel(): Unit = {

    // }
}
