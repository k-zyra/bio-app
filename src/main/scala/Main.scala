package app

/* External imports */

/* Internal imports */
import misc.Console



object BioApp {
	def main(args: Array[String]): Unit = {
		Console.parseArgs(args)

		if (Console.properlyConfigured) {
			println("Doing some operations")
		}

		Console.exiting()
	}
}