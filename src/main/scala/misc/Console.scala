package misc

/* External imports */
import scala.io.StdIn
import scopt.OParser


case class ConsoleConfig (
        operation: String = "",
        files: Seq[String] = Seq(),
        verbose: Boolean = true
    )


object Console {
    var operation: String = ""
    var properlyConfigured: Boolean = false
    private var inputFiles: Seq[String] = Seq()


    private val builder = OParser.builder[ConsoleConfig]
    private val parser = {
        import builder._
        OParser.sequence(
            programName("BioApp"),
            head("BioApp", "1.0"),
            opt[String]('o', "operation")
              .required()
              .valueName("<operation>")
              .action((x, c) => c.copy(operation = x))
              .text("Specifies an operation to be performed (required)."),
            opt[Seq[String]]('f', "files")
              .required()
              .valueName("<file1>,<file2>...")
              .unbounded()
              .action((x, c) => c.copy(files = c.files ++ x))
              .text("Files to be analyzed (required)."),
            opt[Unit]("verbose")
              .action((_, c) => c.copy(verbose = true))
              .text("verbose is a flag"),
            help("help").text("Displays help.")
        )
    }


    def parseArgs(args: Array[String]): Unit = {
        OParser.parse(this.parser, args, ConsoleConfig()) match {
            case Some(config) => {
                if (config.verbose) println("[INFO] Running in verbose mode.")
                if (config.files.nonEmpty) {
                    this.inputFiles = config.files

                    print("Analyzing files: ")
                    println(this.inputFiles.mkString(" "))
                }
                if (config.operation.nonEmpty) {
                    this.operation = config.operation
                    println(s"[INFO] Running operation: ${this.operation}")
                }

            }
            case None => println("An error occurred")
        }
    }


    def exiting(): Unit = {
        StdIn.readLine("Press any key to exit...")
    }
}
