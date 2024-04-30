package examples

/* Internal imports */
import bio.align.multiple.{Config, Fitness, GeneticAlgorithm}
import misc.Constants



object MsaExample {
    val shortSequences: Array[String] =  Array("BBCACBAB", "CCAAABAC", "CCAABBAB", "CBCACBAB")
    val mediumSequences: Array[String] = Array("CATTCAC", "CTCGCAGC", "CTCGCAGC")
    val longSequences: Array[String] = Array("TCAGGATGAAC", "ATCACGATGAACC", "ATCAGGAATGAATCC", "TCAGGAATGAATCGCM")


    def exactMethod(sequences: Array[String]): Unit = {

    }


    def heuristicMethod(sequences: Array[String]): Unit = {
        Config.set(_maxNumberOfEpochs= 2000, _maxEpochsInPlateau = 1800, _generationSize = 300)
        GeneticAlgorithm.prepareInitialPoint(sequences)

        val mutant = GeneticAlgorithm.start(sequences, 1, false)
        mutant.head.foreach(println)

        val solutionSpeciman = mutant.head
        println(s"Mutant alignment score: ${Fitness.getAlignmentCost(solutionSpeciman)}")
    }

    def runExample(sequences: Array[String]): Unit = {
        val exactStart: Long = System.nanoTime()
        this.exactMethod(sequences)
        val exactDuration: Float = (System.nanoTime() - exactStart) / Constants.NanoInMillis
        println(s"Time spent in heuristic duration: ${exactDuration} ms")

        val heuristicStart: Long = System.nanoTime()
        this.heuristicMethod(sequences)
        val heuristicDuration: Float = (System.nanoTime() - heuristicStart)/Constants.NanoInMillis
        println(s"Time spent in heuristic duration: ${heuristicDuration} ms")
    }


    def runRealisticExample(): Unit = {

    }


    def main(args: Array[String]): Unit = {
        if (args.length != 1) {
            println("[ERROR] Incorrect number of arguments.")
            println("Usage: examples.MsaExample [mode]")
        }

        val mode: String = args(0)
        mode match {
            case "short" => this.runExample(this.shortSequences)
            case "medium" => this.runExample(this.mediumSequences)
            case "long" => this.runExample(this.longSequences)
            case "realistic" => this.runRealisticExample()
            case _ => {
                println(s"[ERROR] Incorrect mode: ${mode}.")
                println("Possible values: [short | medium | long | realistic]")
            }
        }
    }

}
