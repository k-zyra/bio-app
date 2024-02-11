package app
 
/* External imports */ 
import edu.umd.marbl.mhap.utils.Utils

/* Internal imports */ 
import misc.Console
import pipeline.CorrectionPhase
import utils._



object BioApp {
	def main(args: Array[String]): Unit = {
		val session = SparkController.getSession()
		val context = SparkController.getContext()

		Console.exiting()
		SparkController.destroy()
	}

}