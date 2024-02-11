package misc

/* External imports */
import java.io.File
import scopt.OParser



object Examples extends Enumeration {
    type Examples = Value
    val NONE, KMERS, GLOBAL, LOCAL, CLUSTERS = Value
}


case class Config(
    input: String = Constants.EmptyString,
    output: String = Constants.EmptyString,

    kmerLen: Int = 0,
    motifLen: Int = 0,
    tandemLen: Int = 0,

    verbose: Boolean = false,
    kwargs: Map[String, String] = Map()
)


class Arguments {
    private val logger = new Logger("Arguments")

    private var inputFiles = Array[String]()
    private var outputFiles = Array[String]()

    private var kmerLength = Array[Int]()
    private var motifLength = Array[Int]()
    private var tandemLength = Array[Int]()

    private var verbose = false
    private var runExample = false

    def setParameters(configuration: Config): Unit = {
        logger.logDebug(f"Configuration received: $configuration")
    }
}


object OptionParser {
    private val parser = new scopt.OptionParser[Config]("scopt") {
        head("BIO-APP", "1.0")

        opt[String]("input")
          .optional()
          .maxOccurs(5)
          .valueName("<string>")
          .action((x, c) => c.copy(input = x))
          .text("Name of the input file(s). Should be in FASTA or FASTQ  format.")


        opt[String]("out")
          .optional()
          .maxOccurs(5)
          .valueName("<string>")
          .action((x, c) => c.copy(output = x))
          .text("Name of the output file.")


        opt[Int]("kmerLen")
          .optional()
          .maxOccurs(5)
          .valueName("<integer>")
          // .action((x, c) => c.copy(output = x))
          .text("Length of generated k-mers.")


        opt[Int]("motifLen")
          .optional()
          .maxOccurs(5)
          .valueName("<integer>")
          // .action((x, c) => c.copy(output = x))
          .text("Length of motifs to be found.")


        opt[Int]("tandemLen")
          .optional()
          .maxOccurs(5)
          .valueName("<integer>")
          // .action((x, c) => c.copy(output = x))
          .text("Length of tandem repeats to be found.")


        opt[Unit]("verbose")
          .action((_, c) => c.copy(verbose = true))
          .text("If set, all logs will be displayed")

    }


    def getParser(): scopt.OptionParser[Config] = {
        return this.parser
    }


    def parseArguments(arguments: Seq[String]): Config = {
        println("Printing...")
        this.parser.parse(arguments, Config()) match {
            case Some(config) =>
                println(arguments)
                println(config)
            case None =>
                println("No arguments specified. Exiting...")
        }

        return Config()
    }


    private def checkStringArgument(arg: String): Boolean = {
        var result = false

        if (arg.endsWith(Constants.FastaExtension) || arg.endsWith(Constants.FastqExtension)) {
            result = true
        }

        return result
    }

}
