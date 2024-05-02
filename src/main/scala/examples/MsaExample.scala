package examples

/* Internal imports */
import bio.align.multiple.{Config, Fitness, GeneticAlgorithm}
import misc.Constants



object MsaExample {
    val shortSequences = Array(
        "TCAGGATGAAC",
        "ATCACGATGAACC",
        "ATCAGGAATGAATCC",
        "TCAGGAATGAATCGCM"
    )
    val mediumSequences = Array(
        "AGCTAGCTAGCTAGC",
        "TCGATCGATCGATCG",
        "AATTCCGGAATTCCG",
        "GATCGATCGATCGAT",
        "CCATGGCCATGGCCA"
    )
    val longSequences = Array(
        "ATCGATCGATCGATCGATCG",
        "TCGCTAGCTAGCTAGCTAGC",
        "GGAATTCCGGAATTCCGCAA",
        "CCATGATCGATCGATCGATC",
        "AGCTAGCTAAGCTAGCTAGC"
    )

    def start(sequences: Array[String]): Unit = {
        GeneticAlgorithm.prepareInitialPoint(sequences)

        val mutant = GeneticAlgorithm.start(sequences, 1, false)
        mutant.head.foreach(println)

        val solutionSpeciman = mutant.head
        println(s"Mutant alignment score: ${Fitness.getAlignmentCost(solutionSpeciman)}")
    }

    def runExample(sequences: Array[String]): Unit = {
        val start: Long = System.nanoTime()
        this.start(sequences)
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis
        println(s"Time spent in heuristic algorithm: ${duration} ms")
    }


    def main(args: Array[String]): Unit = {
        if (args.length != 1) {
            println("[ERROR] Incorrect number of arguments.")
            println("Usage: examples.MsaExample [mode]")
        }

        Config.set(_maxNumberOfEpochs= 2000,
                    _maxEpochsInPlateau = 2000,
                    _generationSize = 200)

        val mode: String = args(0)
        mode match {
            case "short" => this.runExample(this.shortSequences)
            case "medium" => this.runExample(this.mediumSequences)
            case "long" => this.runExample(this.longSequences)
            case _ => {
                println(s"[ERROR] Incorrect mode: ${mode}.")
                println("Possible values: [short | medium | long]")
            }
        }
    }
}
