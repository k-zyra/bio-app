package bio.align.multiple

/* External imports */
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import scala.util.control.Breaks.{break, breakable}

/* Internal imports */
import misc.{Constants, Logger}
import types.Biotype.{Alignment, CurrentAlignment, CurrentPopulation}



object CrossoverMethod extends Enumeration {
    type CrossoverMethod = Value
    val UNDEF, ONE_POINT, UNIFORM, SEQUENTIAL  = Value
}

object SelectionMethod extends Enumeration {
    type SelectionMethod = Value
    val UNDEF, DETERMINISTIC, RANDOM_WHEEL, STOCHASTIC = Value
}

object Crossover {
    private val logger = new Logger("MSA_Crossover")

    private val selectionProbability: mutable.Map[SelectionMethod.Value, Double] = mutable.Map(
        SelectionMethod.DETERMINISTIC -> 0.0,
        SelectionMethod.RANDOM_WHEEL -> 0.0,
        SelectionMethod.STOCHASTIC -> 0.0,
    ).withDefaultValue(1/3)

    private val selectionFrequency: mutable.Map[SelectionMethod.Value, Int] = mutable.Map(
        SelectionMethod.DETERMINISTIC -> 0,
        SelectionMethod.RANDOM_WHEEL -> 0,
        SelectionMethod.STOCHASTIC -> 0,
    ).withDefaultValue(0)


    private val methodProbability: mutable.Map[CrossoverMethod.Value, Double] = mutable.Map(
        CrossoverMethod.ONE_POINT -> 0.0,
        CrossoverMethod.UNIFORM -> 0.0,
        CrossoverMethod.SEQUENTIAL -> 0.0,
    ).withDefaultValue(1/3)

    private val methodFrequency: mutable.Map[CrossoverMethod.Value, Int] = mutable.Map(
        CrossoverMethod.ONE_POINT -> 0,
        CrossoverMethod.UNIFORM -> 0,
        CrossoverMethod.SEQUENTIAL -> 0,
    ).withDefaultValue(1/3)


    def resetProbabilities(): Unit = {
        selectionProbability(SelectionMethod.DETERMINISTIC) = 0.4
        selectionProbability(SelectionMethod.RANDOM_WHEEL) = 0.4
        selectionProbability(SelectionMethod.STOCHASTIC) = 0.2

        methodProbability(CrossoverMethod.ONE_POINT) = 0.75
        methodProbability(CrossoverMethod.UNIFORM) = 0.05
        methodProbability(CrossoverMethod.SEQUENTIAL) = 0.2
    }


    def resetFrequencies(): Unit = {
        selectionFrequency.transform((_, _) => 0)
        methodFrequency.transform((_, _) => 0)
    }


    def setupMutationSettings(): Unit = {
        this.resetFrequencies()
        this.resetProbabilities()
    }


    /* Adjust possibilities of certain selection methods, based on the evolution progress
    */
    def reevaluteSelectionProbabilties(population: CurrentPopulation): Unit = {

    }


    /* Adjust possibilities of choosing certain crossover method, based on the evolution progress
    */
    def reevaluteMethodProbabilties(population: CurrentPopulation): Unit = {
        // TBD
    }


    /*  Choose parents in a deterministic manner
    *
    *   Parents are selected from given population base using random wheel selection
    *   with probability proportional to their fitness
    */
    def deterministicSelection(populationBase: CurrentPopulation,
                               numberOfParents: Int): CurrentPopulation = {
        Fitness.getFittestSpecies(populationBase, numberOfParents)
    }


    /*  Choose parents in a stochastic manner
    *
    *   Parents are selected from given population base using random wheel selection
    *   with probability proportional to their fitness
    */
    def randomWheelSelection(populationBase: CurrentPopulation,
                             numberOfParents: Int): CurrentPopulation = {
        val parents: CurrentPopulation = new CurrentPopulation
        var choices: ArrayBuffer[Double] = new ArrayBuffer[Double]()

        var cumulativeScore: Long = 0
        val scores: ArrayBuffer[Int] = new ArrayBuffer[Int]()

        for (speciman <- populationBase) {
            val specimanScore: Int = Fitness.getAlignmentCost(speciman)
            scores += specimanScore
            cumulativeScore += specimanScore
        }

        for (_ <- 0 to numberOfParents/2) choices += Random.nextDouble()
        choices = choices.sorted

        var iter: Int = 0
        var getNext: Boolean = false
        for (choice <- choices) {
            getNext = false

            while(!getNext) {
                if (choice <= scores(iter)) {
                    parents += populationBase(iter)
                    getNext = true
                } else {
                    iter += 1
                }
            }
        }

        parents
    }


    /*  Choose parents in a stochastic manner
    *   Parents are selected from given population base randomly, with equal probabilities
    */
    def stochasticSelection(populationBase: CurrentPopulation,
                            numberOfParents: Int): CurrentPopulation = {
        val parents: CurrentPopulation = new CurrentPopulation
        val parentIds =  Seq.fill(numberOfParents)(Random.nextInt(populationBase.length))
        for (id <- parentIds) parents += populationBase(id)

        parents
    }


    /*  Choose method for selecting parents
    */
    def selection(populationBase: CurrentPopulation,
                  numberOfParents: Int): CurrentPopulation = {
        var parents: CurrentPopulation = new CurrentPopulation

        val choice: Double = Random.nextDouble()
        if (choice < selectionProbability(SelectionMethod.STOCHASTIC)) {
            parents = this.stochasticSelection(populationBase, numberOfParents)
            this.selectionFrequency(SelectionMethod.STOCHASTIC) += 1
        } else if (choice < selectionProbability(SelectionMethod.RANDOM_WHEEL)) {
            parents = this.randomWheelSelection(populationBase, numberOfParents)
            this.selectionFrequency(SelectionMethod.RANDOM_WHEEL) += 1
        } else {
            parents = this.deterministicSelection(populationBase, numberOfParents)
            this.selectionFrequency(SelectionMethod.DETERMINISTIC) += 1
        }

        parents
    }


    /*  Perform a one-point crossover by merging parents' alignments
    *   Return only the best child
    */
    def onePoint(firstParent: Alignment,
              secondParent: Alignment,
              verbose: Boolean = logger.isVerbose()): Alignment = {
        assert(firstParent.length == secondParent.length,
                s"Parents are not of equal length, ${firstParent.length} vs ${secondParent.length}")

        val numberOfAlignments: Integer = firstParent.length
        val maxCutPosition: Integer = firstParent(0).count(_ != '-') - 1
        val cutPoint: Integer = Random.nextInt(maxCutPosition) + 1

        val firstChild: ArrayBuffer[String] = ArrayBuffer[String]()
        val secondChild: ArrayBuffer[String] = ArrayBuffer[String]()

        val start = System.nanoTime()
        for (i <- 0 until numberOfAlignments) {
            val firstSequence: String = firstParent(i)
            val secondSequence: String = secondParent(i)
            val ids = new ArrayBuffer[Int]()

            for (sequence <- Array(firstSequence, secondSequence)) {
                var tmpCutpoint = cutPoint

                breakable {
                    for (id <- 0 until sequence.length()) {
                        if (sequence(id) != '-') tmpCutpoint -= 1
                        if (tmpCutpoint == 0) {
                            ids += id
                            break
                        }
                    }
                }
            }

            val firstCutPoint: Int = ids(0)
            val secondCutPoint: Int = ids(1)

            val firstChildSequence = firstSequence.take(firstCutPoint) + secondSequence.substring(secondCutPoint)
            val secondChildSequence = secondSequence.take(secondCutPoint) + firstSequence.substring(firstCutPoint)

            firstChild += firstChildSequence
            secondChild += secondChildSequence
        }
        val duration: Float = (System.nanoTime() - start) / Constants.NanoInMillis

        val firstChildArray: Alignment = Utils.adjustAlignment(firstChild.toArray)
        val secondChildArray: Alignment = Utils.adjustAlignment(secondChild.toArray)

        if (verbose) logger.logInfo(f"One-point crossover duration: ${duration} ns")
        Fitness.chooseChild(firstChildArray, secondChildArray)
    }


    /* Find common points in sequences
    */
    private def findCutPoints(specimen: Array[String]): Array[Int] = {
        val referenceString: String = specimen(0)

        referenceString.indices.filter { index =>
            specimen.forall(_.charAt(index) == referenceString.charAt(index))
        }.toArray
    }


    /* Modify species using uniform crossover
    *  Due to its characteristic, it should be used at the end of evolution
    */
    def uniform(firstParent: Alignment,
                secondParent: Alignment,
                verbose: Boolean = logger.isVerbose()): Alignment = {
        assert(firstParent.length == secondParent.length)
        val numberOfSequences: Int = firstParent.length
        val firstChild: CurrentAlignment = new CurrentAlignment
        val secondChild: CurrentAlignment = new CurrentAlignment

        val start: Long = System.nanoTime()
        val commonPoints: Array[Int] = this.findCutPoints(firstParent ++ secondParent)
        if (commonPoints.length < 2) println("Cannot find common points")
        val cutPoints = Utils.randomSample(commonPoints, 2).sorted

        val firstId: Int = cutPoints(0)
        val secondId: Int = cutPoints(1)

        for (i <- 0 until numberOfSequences) {
            firstChild += firstParent(i).take(firstId) + secondParent(i).substring(firstId, secondId) + firstParent(i).substring(secondId)
            secondChild += secondParent(i).take(firstId) + firstParent(i).substring(firstId, secondId) + secondParent(i).substring(secondId)
        }

        val firstChildArray: Alignment = firstChild.toArray
        val secondChildArray: Alignment = secondChild.toArray

        val duration: Double = (System.nanoTime() - start)/Constants.NanoInMillis
        if (verbose) logger.logInfo(s"Uniform crossover duration: ${duration} ms")
        Fitness.chooseChild(firstChildArray, secondChildArray)
    }


    /* Modify species by swapping single sequence between them
    */
    def sequential(firstParent: Alignment,
                   secondParent: Alignment,
                   verbose: Boolean = logger.isVerbose()): Alignment = {
        val numberOfSequences: Int = firstParent.length - 1
        val sequenceId: Int = Random.nextInt(numberOfSequences) + 1
        val storage: String = firstParent(sequenceId)

        val firstChild: ArrayBuffer[String] = new ArrayBuffer[String]()
        val secondChild: ArrayBuffer[String] = new ArrayBuffer[String]()

        firstChild ++= firstParent
        secondChild ++= secondParent

        firstChild(sequenceId) = secondChild(sequenceId)
        secondChild(sequenceId) = storage

        val firstChildArray: Alignment = Utils.adjustAlignment(firstChild.toArray)
        val secondChildArray: Alignment = Utils.adjustAlignment(secondChild.toArray)

        Fitness.chooseChild(firstChildArray, secondChildArray)
    }


    /*  Choose crossover method
    */
    def chooseMethod(): CrossoverMethod.CrossoverMethod = {
        var crossoverMethod: CrossoverMethod.CrossoverMethod = CrossoverMethod.UNDEF

        val choice: Double = Random.nextDouble()
        if (choice < methodProbability(CrossoverMethod.ONE_POINT)) {
            crossoverMethod = CrossoverMethod.ONE_POINT
            this.methodFrequency(CrossoverMethod.ONE_POINT) += 1
        } else if (choice < methodProbability(CrossoverMethod.UNIFORM)) {
            crossoverMethod = CrossoverMethod.UNIFORM
            this.methodFrequency(CrossoverMethod.UNIFORM) += 1
        } else {
            crossoverMethod = CrossoverMethod.SEQUENTIAL
            this.methodFrequency(CrossoverMethod.SEQUENTIAL) += 1
        }

        crossoverMethod
    }


    /*  Perform crossover stage
    */
    def run(population: CurrentPopulation): CurrentPopulation = {
        val parents = this.selection(population, Config.reproductionSize * 2)
        val children: CurrentPopulation = new CurrentPopulation

        val crossoverMethod: CrossoverMethod.CrossoverMethod = this.chooseMethod()
        crossoverMethod match {
            case CrossoverMethod.ONE_POINT => {
                for (id <- parents.indices by 2) children += Crossover.onePoint(parents(id), parents(id + 1), verbose = false)
            }
            case CrossoverMethod.UNIFORM => {
                for (id <- parents.indices by 2) children += Crossover.uniform(parents(id), parents(id + 1), verbose = false)
            }
            case CrossoverMethod.SEQUENTIAL => {
                for (id <- parents.indices by 2) children += Crossover.sequential(parents(id), parents(id + 1), verbose = false)
            }
        }

        children
    }
}
