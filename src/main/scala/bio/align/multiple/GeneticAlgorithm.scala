package bio.align.multiple

/* External imports */
import types.DistanceType
import types.DistanceType.DistanceType

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/* Internal imports */
import misc.{Constants, Logger}
import types.Biotype.{Alignment, CurrentPopulation, Population}



object GeneticAlgorithm {
    private val logger = new Logger("MSA_GeneticAlgorithm")


    /* Reset algorithm variables
    */
    def reset(verbose: Boolean = logger.isVerbose()): Unit = {
        Config.reset()
        Mutation.setupMutationSettings()

        if (verbose) logger.logInfo("Reset")
    }


    /* Verify whether the conditions are met
    */
    private def checkEndCondition(population: CurrentPopulation): Unit = {
        if (Config.epochsInPlateau == Config.reconfigAtEpoch) {
            println("Should reevaluate probabilities")
            Mutation.reevaluteProbabilties(population)
        }

        if (Config.epoch == Config.maxEpoch || Config.epochsInPlateau == Config.maxEpochsInPlateau) {
            if (logger.isVerbose()) logger.logWarn(
                s"Breaking loop at epoch: ${Config.epoch}, epochs in plateau: ${Config.epochsInPlateau}")
            Config.keepGoing = false
        }
    }


    /* Sort sequences by their similarity to the other sequences in alignment in decreasing order
    *  Levenstein distance is chosen as a default measure as sequences can be of different length
    */
    def prepareInitialPoint(sequences: Array[String],
                            similarityMeasure: DistanceType = DistanceType.LEVENSHTEIN): Array[String] = {
        val distances = sequences.zip(sequences.map(Fitness.getAverageDistance(_, sequences, similarityMeasure))).sortBy(-_._2)
        return distances.map(_._1).toArray
    }


    /* Generate an initial generation G0 for the genetic algorithm 
    *
    * This method requires the following parameters:
    *   generation size - number of species in a generation
    *   sequence length - number of residues and gaps in sequences, all sequences are of the same length
    *   alignment size - number of sequences in a single alignment  
    * 
    *  Return array of alignments 
    */
    def generateInitialGeneration(sequences: Array[String],
                                generationSize: Integer = Config.generationSize,
                                maxOffset: Integer = Config.maxOffset,
                                verbose: Boolean = logger.isVerbose()): Population = {
        val generation: ArrayBuffer[Alignment] = ArrayBuffer[Alignment]()

        val start: Long = System.nanoTime()
        for (_ <- 0 to generationSize) {
            val tempSpecimen: ArrayBuffer[StringBuilder] = ArrayBuffer[StringBuilder]()

            for (sequence <- sequences) {
                val offset: Integer = Random.nextInt(maxOffset + 1)
                val offsetString = "-" * offset
                tempSpecimen += new StringBuilder(offsetString + sequence)
            }

            val maxLength: Int = tempSpecimen.maxBy(_.length).length
            val specimen: ArrayBuffer[String] = new ArrayBuffer[String]()

            for (sequence <- tempSpecimen) {
                val seqLen = sequence.length
                specimen += sequence + ("-" * (maxLength-seqLen))
            }

            generation += specimen.result().toArray
            specimen.clear()
            tempSpecimen.clear()
        }

        Config.initialAverageLength = Utils.getAverageLength(generation)

        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis
        if (verbose) logger.logInfo(f"Time spent in <generateInitialGeneration> ${duration} ms")

        return generation.result.toArray
    }


    /* Generating new species using mutations
    */
    private def mutation(population: CurrentPopulation,
                         verbose: Boolean = logger.isVerbose()): CurrentPopulation = {
        assert(population.size < Config.generationSize,
            s"Population size should be less than ${Config.generationSize}, actual: ${population.size}")

        val start = System.nanoTime()
        val extendedPopulation = Mutation.run(population)
        val duration: Float = (System.nanoTime() - start) / Constants.NanoInMillis

        if (verbose) logger.logInfo(s"Create new ${Config.reproductionSize} mutants in time: ${duration} ms.")
        return extendedPopulation
    }


    /* Generating new species using crossover
    */
    private def breeding(population: CurrentPopulation,
                         verbose: Boolean = logger.isVerbose()): CurrentPopulation = {
        assert(population.size < Config.generationSize,
                s"Population size should be less than ${Config.generationSize}, actual: ${population.size}")

        val start = System.nanoTime()
        val children: CurrentPopulation = Crossover.run(population)
        val duration: Float = (System.nanoTime() - start) / Constants.NanoInMillis

        if (verbose) logger.logInfo(s"Create new ${Config.reproductionSize} children in time: ${duration} ms.")
        return children
    }


    /* Start the algorithm
    */
    def start(sequences: Array[String],
              numberOfSolutions: Int = 1,
              verbose: Boolean = logger.isVerbose()): Array[Alignment] = {
        var data: Array[String] = sequences

        this.reset()
        Config.isSet()

        if (Config.preprocess) data = this.prepareInitialPoint(sequences)
        var population: CurrentPopulation = this.generateInitialGeneration(sequences, verbose = true).to[ArrayBuffer]

        val rankedInitial = Fitness.rankAlignments(population)
        val initialBestScore = rankedInitial.head._1
        val initialBest = population(rankedInitial.head._2)
        Config.currentBest = initialBestScore

        if (verbose) {
            logger.logInfo(s"Initial best alignment score: ${initialBestScore}")
            initialBest.foreach(println)
        }

        while (Config.keepGoing) {
            population = Fitness.getFittestSpecies(population, Config.replacementSize)
            population ++= this.breeding(population, verbose = false)
            population = this.mutation(population, verbose = false)

            Config.epoch += 1
            this.checkEndCondition(population)

            val ranked = Fitness.rankAlignments(population)
            val epochBestScore = ranked.head._1

            if (epochBestScore > Config.currentBest) {
                Config.currentBest = epochBestScore
                Config.epochsInPlateau = 0
                if (verbose) logger.logInfo(s"New best score: ${Config.currentBest}")
            } else {
                Config.epochsInPlateau += 1
                if (verbose) logger.logInfo(s"Epochs in plateau: ${Config.epochsInPlateau}, current best: ${Config.currentBest}")
            }
        }

        return Fitness.getFittestSpecies(population, numberOfSolutions).toArray
    }
}