package bio.align.multiple

/* External imports */
import org.sparkproject.dmg.pmml.Similarity
import org.sparkproject.dmg.pmml.text.TextModelSimiliarity.SimilarityType
import types.DistanceType
import types.DistanceType.DistanceType

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/* Internal imports */
import misc.{Constants, Logger}
import types.Biotype.{Alignment, CurrentPopulation, Population}



object GeneticAlgorithm {
    private val logger = new Logger("MSA_GeneticAlgorithm")

    private var isConfigured: Boolean = false
    private var preprocess: Boolean = true

    private var epoch: Int = 0
    private var epochsInPlateau: Int = 0
    private var currentBest: Int = Int.MinValue

    private var maxEpoch: Int = 50
    private var keepGoing: Boolean = false

    private var maxOffset: Int = 0
    private var maxExpectedOffspring: Int = 0

    private var generationSize: Int = 0
    private var replacementSize: Int = 0
    private var reproductionSize: Int = 0
    private var mutationSize: Int = 0



    /* Reset algorithm variables
    */
    def reset(verbose: Boolean = logger.isVerbose()): Unit = {
        this.epoch = 0
        this.keepGoing = true

        if (verbose) logger.logInfo("Reset")
    }


    /* Configure algorithm settings
    */
    def configure(_maxNumberOfEpochs: Int = 100,
                   _generationSize: Int = 100,
                  _maxOffset: Int = 5,
                  _replacementFactor: Double = 0.5,
                  _reproductionFactor: Double = 0.5,
                  _maxExpectedOffspring: Int = 2,
                  _preprocess: Boolean = true,
                  verbose: Boolean = logger.isVerbose()): Unit = {
        this.generationSize = _generationSize
        this.maxOffset = _maxOffset
        this.replacementSize = (_replacementFactor * this.generationSize).toInt
        this.reproductionSize = (_reproductionFactor * (this.generationSize - this.replacementSize)).toInt
        this.mutationSize = this.replacementSize - this.reproductionSize
        this.maxExpectedOffspring = _maxExpectedOffspring
        this.maxEpoch = _maxNumberOfEpochs

        this.preprocess = _preprocess
        this.isConfigured = true
        if (verbose) logger.logInfo("Configuration done.")
    }


    /* Verify whether the conditions are met
    */
    private def checkIfConfigured(): Unit = {
        if (this.isConfigured) {
            logger.logInfo("Continuing with given configuration.")
        } else {
            this.configure()
            logger.logWarn("Configured using default values.")
        }
    }


    /* Verify whether the conditions are met
    */
    private def checkEndCondition(): Unit = {
        if (this.epoch == 200
            || this.epochsInPlateau > 5) this.keepGoing = false

//        if (this.epoch == this.maxEpoch) this.keepGoing = false
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
                                generationSize: Integer = this.generationSize,
                                maxOffset: Integer = this.maxOffset,
                                verbose: Boolean = logger.isVerbose()): Population = {
        val generation: ArrayBuffer[Alignment] = ArrayBuffer[Alignment]()

        val start: Long = System.nanoTime()
        for (_ <- 0 to generationSize) {
            val tempSpecimen: ArrayBuffer[StringBuilder] = ArrayBuffer[StringBuilder]()
            val test: ArrayBuffer[String] = new ArrayBuffer[String]()

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
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) logger.logInfo(f"Time spent in <generateInitialGeneration> ${duration} ms")
        return generation.result.toArray
    }


    /* Generating new species using mutations
    */
    private def mutation(population: CurrentPopulation,
                         verbose: Boolean = logger.isVerbose()): CurrentPopulation = {
        assert(population.size < this.generationSize,
            s"Population size should be less than ${this.generationSize}, actual: ${population.size}")
//        val numberOfParents: Int = population.size
        val extendedPopulation: CurrentPopulation = population

        val start = System.nanoTime()
        for (_ <- 0 to this.reproductionSize) {
//            val parentId = Random.nextInt(numberOfParents)
//            extendedPopulation += this.getRandomMutation(population(parentId))
            extendedPopulation += Mutation.stochastic(population)
        }
        val duration: Float = (System.nanoTime() - start) / Constants.NanoInMillis

        if (verbose) logger.logInfo(s"Create new ${this.reproductionSize} mutants in time: ${duration} ms.")
        return extendedPopulation
    }


//    /* Generating new species using crossovers (one-point or uniform)
//    */
//    private def breeding_bak(population: CurrentPopulation,
//                         verbose: Boolean = logger.isVerbose()): CurrentPopulation = {
//        assert(population.size < this.generationSize)
//        val numberOfParents: Int = population.length
//        val extendedPopulation: CurrentPopulation = population
//
//        val start = System.nanoTime()
//        for (i <- 0 to this.reproductionSize) {
//            val parentId: Seq[Int] = Seq.fill(2)(Random.nextInt(numberOfParents))
//            extendedPopulation += Crossover.onePoint(population(parentId(0)), population(parentId(1)), verbose = false)
//        }
//        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis
//
//        if (verbose) logger.logInfo(s"Create new ${this.reproductionSize} children in time: ${duration} ms.")
//        return extendedPopulation
//    }

    private def breeding(population: CurrentPopulation,
                         verbose: Boolean = logger.isVerbose()): CurrentPopulation = {
        assert(population.size < this.generationSize,
                s"Population size should be less than ${this.generationSize}, actual: ${population.size}")
        val children: CurrentPopulation = new CurrentPopulation

        val start = System.nanoTime()
        val parents = Crossover.selection(population, this.reproductionSize * 2)

        for (id <- parents.indices by 2) {
            children += Crossover.onePoint(parents(id), parents(id+1))
        }

//        for (i <- 0 to this.reproductionSize) {
//            val parentId: Seq[Int] = Seq.fill(2)(Random.nextInt(numberOfParents))
//            extendedPopulation += Crossover.onePoint(population(parentId(0)), population(parentId(1)), verbose = false)
//        }
        val duration: Float = (System.nanoTime() - start) / Constants.NanoInMillis

//        if (verbose) logger.logInfo(s"Create new ${this.reproductionSize} children in time: ${duration} ms.")
        return children
    }

    /* Start the algorithm
    */
    def start(sequences: Array[String],
              numberOfSolutions: Int = 1,
              verbose: Boolean = logger.isVerbose()): Array[Alignment] = {
        var data: Array[String] = sequences

        this.reset()
        this.checkIfConfigured()
//        if (this.preprocess) data = this.prepareInitialPoint(sequences)
        data = this.prepareInitialPoint(sequences)
        data.foreach(println)

        var population: CurrentPopulation = this.generateInitialGeneration(sequences, verbose = true).to[ArrayBuffer]

        val rankedInitial = Fitness.rankAlignments(population)
        val initialBestScore = rankedInitial.head._1
        val initialBest = population(rankedInitial.head._2)
        this.currentBest = initialBestScore

        if (verbose) {
            logger.logInfo(s"Initial best alignment score: ${initialBestScore}")
            initialBest.foreach(println)
        }

        while (this.keepGoing) {
            population = Fitness.getFittestSpecies(population, this.replacementSize)
            population ++= this.breeding(population, verbose = false)
            population = this.mutation(population, verbose = false)

            this.epoch += 1
            this.checkEndCondition()
        }

        return Fitness.getFittestSpecies(population, numberOfSolutions).toArray
    }
}