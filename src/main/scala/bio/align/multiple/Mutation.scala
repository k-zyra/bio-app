package bio.align.multiple

/* External imports */
import scala.collection.mutable
import scala.util.Random

/* Internal imports */
import misc.Logger
import types.Biotype.{Alignment, CurrentPopulation}



/**
 *  ISG - insert single gap
 *  IG  - insert gap
 *  RG  - remove gap
 *  RGB - remove gap block
 *  TRG - trim redundant gaps
 *  MSG - move single gap
 */
object MutationOperator extends Enumeration {
    type MutationOperator = Value
    val UNDEF, ISG, IG, RG, RGB, TRG, MSG = Value
}


object Mutation {
    private val logger = new Logger("MSA_Mutation")

    private val probability: mutable.Map[MutationOperator.Value, Double] = mutable.Map(
        MutationOperator.ISG -> 0.0,
        MutationOperator.IG -> 0.0,
        MutationOperator.RG -> 0.0,
        MutationOperator.RGB -> 0.0,
        MutationOperator.MSG -> 0.0,
        MutationOperator.TRG -> 0.0
    ).withDefaultValue(1/6)


    private val frequency: mutable.Map[MutationOperator.Value, Int] = mutable.Map(
        MutationOperator.ISG -> 0,
        MutationOperator.IG -> 0,
        MutationOperator.RG -> 0,
        MutationOperator.RGB -> 0,
        MutationOperator.TRG -> 0,
        MutationOperator.MSG -> 0
    ).withDefaultValue(0)



    def resetProbabilities(): Unit = {
        probability(MutationOperator.ISG) = 0.3
        probability(MutationOperator.IG) = 0.3
        probability(MutationOperator.RG) = 0.1
        probability(MutationOperator.RGB) = 0.1
        probability(MutationOperator.MSG) = 0.1
        probability(MutationOperator.TRG) = 0.1
    }


//    def resetProbabilities(): Unit = {
//        val initialProbability: Double = 1.0/probability.size.toDouble
//        this.setInitialProbabilities()
//        probability.transform((_, _) => initialProbability)
//    }


    def resetFrequencies(): Unit = {
        frequency.transform((_, _) => 0)
    }


    def setupMutationSettings(): Unit = {
        this.resetFrequencies()
        this.resetProbabilities()
    }


    /* Adjust possibilities of certain mutations, based on the evolution progress
    */
    def reevaluteProbabilties(population: CurrentPopulation): Unit = {
        var totalDensity: Double = 0.0
        val averageSolutionLength: Int = Utils.getAverageLength(Random.shuffle(population.result()).take(10))
        val relativeLength: Double = averageSolutionLength.toDouble/Config.initialAverageLength.toDouble

        println("Probabilities before modification")
        probability.foreach(println)
        println()

        println(s"Epoch: ${Config.epoch}")
        println(s"Evolution progress: ${Config.getEvolutionProgress()} ")

        println(s"Average solution length: ${averageSolutionLength}")
        println(s"Length at the beginning: ${Config.initialAverageLength}")
        println(s"Relative length: ${relativeLength}")

        if (Config.isEndingStage()) {
            probability(MutationOperator.TRG) *= 0.5
            println(s"Trim redundant gaps prob increased to: ${probability(MutationOperator.TRG)}")
        }
        totalDensity += probability(MutationOperator.TRG)

        var insertGapsWeight: Double = Config.getEvolutionProgress() * relativeLength
        if (insertGapsWeight > 1) insertGapsWeight = scala.math.log10(insertGapsWeight)
        println(s"Inserting gaps weight: ${insertGapsWeight}")

        // Chances of inserting gaps are decreasing with number of epochs and solution length
        probability(MutationOperator.ISG) *= (1 - insertGapsWeight * 0.75)
        probability(MutationOperator.IG) *= (1 - insertGapsWeight)

        totalDensity += probability(MutationOperator.ISG)
        totalDensity += probability(MutationOperator.IG)

        // Chances of removing gaps are increasing with number of epochs and solution length
        probability(MutationOperator.RGB) *= (1 + insertGapsWeight)
        probability(MutationOperator.RG) *= (1 + insertGapsWeight)

        totalDensity += probability(MutationOperator.RGB)
        totalDensity += probability(MutationOperator.RG)

        // Probability of moving gaps depends on the probabilities of the other mutations
        probability(MutationOperator.MSG) *= scala.math.log10(Config.epoch)
        totalDensity += probability(MutationOperator.MSG)

        println(s"Total density: ${totalDensity}")
        probability.transform((_, value) => value/totalDensity)

        println()
        println("Probability after mutation:")
        probability.foreach(println)
    }


    /* Choose random mutation from all implemented, based on assigned probability
    */
    def stochastic(population: CurrentPopulation): Alignment = {
        var parentId: Int = Random.nextInt(population.size)
        var parent: Alignment = population(parentId)
        var mutant: Alignment = parent.clone()

        var attempts: Int = 0
        while (parent sameElements mutant) {
            val choice: Double = Random.nextDouble()

            if (choice < probability(MutationOperator.ISG)) {
                this.frequency(MutationOperator.ISG) += 1
                mutant = GapMutation.insertSingleGap(parent)
            } else if (choice < probability(MutationOperator.IG)) {
                this.frequency(MutationOperator.IG) += 1
                mutant = GapMutation.insertGap(parent)
            } else if (choice < probability(MutationOperator.RG)) {
                this.frequency(MutationOperator.RG) += 1
                mutant = GapMutation.removeGap(parent)
            } else if (choice < probability(MutationOperator.TRG)) {
                this.frequency(MutationOperator.TRG) += 1
                mutant = GapMutation.trimRedundantGaps(parent)
            } else if (choice < probability(MutationOperator.RGB)) {
                this.frequency(MutationOperator.RGB) += 1
                mutant = GapMutation.removeGapBlock(parent)
            } else {
                this.frequency(MutationOperator.MSG) += 1
                mutant = GapMutation.moveSingleGap(parent)
            }

            attempts += 1
            if (attempts == 5) {
                parentId = Random.nextInt(population.size)
                parent = population(parentId)
                attempts = 0
            }
        }

        return mutant
    }


    /* Choose random mutation from all implemented, based on assigned probability
    */
    def uniform(population: CurrentPopulation): Alignment = {
        var parentId: Int = Random.nextInt(population.size)
        var parent: Alignment = population(parentId)
        var mutant: Alignment = parent.clone()

        var attempts: Int = 0
        while (parent sameElements mutant) {
            val choice: Int = Random.nextInt(6)

            if (choice == 0) {
                this.frequency(MutationOperator.ISG) += 1
                mutant = GapMutation.insertSingleGap(parent)
            } else if (choice == 1) {
                this.frequency(MutationOperator.IG) += 1
                mutant = GapMutation.insertGap(parent)
            } else if (choice == 2) {
                this.frequency(MutationOperator.RG) += 1
                mutant = GapMutation.removeGap(parent)
            } else if (choice == 3) {
                this.frequency(MutationOperator.TRG) += 1
                mutant = GapMutation.trimRedundantGaps(parent)
            } else if (choice == 4) {
                this.frequency(MutationOperator.RGB) += 1
                mutant = GapMutation.removeGapBlock(parent)
            } else {
                this.frequency(MutationOperator.MSG) += 1
                mutant = GapMutation.moveSingleGap(parent)
            }

            attempts += 1
            if (attempts == 5) {
                parentId = Random.nextInt(population.size)
                parent = population(parentId)
                attempts = 0
            }
        }

        return mutant
    }


    def run(population: CurrentPopulation): CurrentPopulation = {
        val extendedPopulation: CurrentPopulation = population

        if (Config.isEarlyStage()) {
            for (_ <- 0 to Config.reproductionSize) {
                extendedPopulation += Mutation.uniform(population)
            }
        } else if (Config.isEndingStage()) {
            for (_ <- 0 to Config.reproductionSize) {
                extendedPopulation += Mutation.stochastic(population)
            }
        } else {
            for (_ <- 0 to Config.reproductionSize) {
                extendedPopulation += Mutation.stochastic(population)
            }
        }

        return extendedPopulation
    }
}
