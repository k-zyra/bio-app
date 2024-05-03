package bio.align.multiple

/* External imports */
import misc.Constants

import scala.collection.mutable
import scala.util.Random

/* Internal imports */
import misc.Logger
import types.Biotype.{Alignment, CurrentPopulation}



/**
 *  ISG - insert single gap
 *  IG  - insert gap
 *  EG  - extend gap
 *  RG  - remove gap
 *  RSG - remove single gap
 *  RGB - remove gap block
 *  TRG - trim redundant gaps
 *  MSG - move single gap
 */
object MutationOperator extends Enumeration {
    type MutationOperator = Value
    val UNDEF, ISG, IG, EG, RG, RSG, RGB, TRG, MSG = Value
}


object Mutation {
    private val logger = new Logger("MSA_Mutation")

    private val probability: mutable.Map[MutationOperator.Value, Double] = mutable.Map(
        MutationOperator.ISG -> 0.0,
        MutationOperator.IG -> 0.0,
        MutationOperator.EG -> 0.0,
        MutationOperator.RG -> 0.0,
        MutationOperator.RSG -> 0.0,
        MutationOperator.RGB -> 0.0,
        MutationOperator.MSG -> 0.0,
        MutationOperator.TRG -> 0.0
    ).withDefaultValue(1/8)


    private val frequency: mutable.Map[MutationOperator.Value, Int] = mutable.Map(
        MutationOperator.ISG -> 0,
        MutationOperator.IG -> 0,
        MutationOperator.EG -> 0,
        MutationOperator.RG -> 0,
        MutationOperator.RSG -> 0,
        MutationOperator.RGB -> 0,
        MutationOperator.TRG -> 0,
        MutationOperator.MSG -> 0
    ).withDefaultValue(0)


    def resetProbabilities(): Unit = {
        probability(MutationOperator.ISG) = 0.35
        probability(MutationOperator.IG) = 0.7
        probability(MutationOperator.EG) = 0.75
        probability(MutationOperator.RG) = 0.8
        probability(MutationOperator.RSG) = 0.85
        probability(MutationOperator.RGB) = 0.9
        probability(MutationOperator.MSG) = 0.95
        probability(MutationOperator.TRG) = 1.0

//        probability(MutationOperator.ISG) = 0.2
//        probability(MutationOperator.IG) = 0.4
//        probability(MutationOperator.EG) = 0.6
//        probability(MutationOperator.RG) = 0.7
//        probability(MutationOperator.RSG) = 0.8
//        probability(MutationOperator.RGB) = 0.85
//        probability(MutationOperator.MSG) = 0.95
//        probability(MutationOperator.TRG) = 1.0
    }


    def resetFrequencies(): Unit = {
        frequency.transform((_, _) => 0)
    }


    def setupMutationSettings(): Unit = {
        this.resetFrequencies()
        this.resetProbabilities()
    }


    def showProbabilities(): Unit = {
        println("\n=== Probabilities ===")
        println(probability.mkString("\n"))
    }

    def displayStatus(): Unit = {
        println(s"Epoch: ${Config.epoch}")
        println(s"Evolution progress: ${Config.getEvolutionProgress()} ")
        println(s"Length at the beginning: ${Config.initialAverageLength}")

        this.showProbabilities()
    }


    /* Adjust possibilities of certain mutations, based on the evolution progress
    */
    def reevaluteProbabilties(population: CurrentPopulation): Unit = {
        this.displayStatus()
        if (Config.dynamicCrossover == Constants.DISABLED) {
            logger.logWarn("Dynamic mutation disabled. Skipping reevaluation")
            return
        }

        var totalDensity: Double = 0.0
        val averageSolutionLength: Int = Utils.getAverageLength(Random.shuffle(population.result()).take(10))
        val relativeLength: Double = averageSolutionLength.toDouble/Config.initialAverageLength.toDouble
        println(s"Average solution length: ${averageSolutionLength}")
        println(s"Relative length: ${relativeLength}")

        if (Config.isEndingStage()) {
            probability(MutationOperator.TRG) *= 0.5
            println(s"Trim redundant gaps prob increased to: ${probability(MutationOperator.TRG)}")
        }

//        var insertGapsWeight: Double = Config.getEvolutionProgress() + relativeLength
        var insertGapsWeight: Double = Config.getEvolutionProgress() * relativeLength
        println(s"Insert gap weight: ${insertGapsWeight}")

        if (insertGapsWeight > 1) insertGapsWeight = scala.math.log10(insertGapsWeight)
        println(s"Inserting gaps weight: ${insertGapsWeight}")

        // Chances of inserting gaps are decreasing with number of epochs and solution length
        probability(MutationOperator.ISG) *= (1 + insertGapsWeight)
        probability(MutationOperator.IG) *= (1 + insertGapsWeight)

        // Chances of removing gaps are increasing with number of epochs and solution length
        probability(MutationOperator.EG) *= (1 + insertGapsWeight * 0.5)

        // We dont need to remove gaps before
        if (Config.isEndingStage()) {
            probability(MutationOperator.RGB) *= (1 + insertGapsWeight)
            probability(MutationOperator.RG) *= (1 + insertGapsWeight)
        }

        // Probability of moving gaps depends on the probabilities of the other mutations
        probability(MutationOperator.MSG) *= (1 + insertGapsWeight)
//        probability(MutationOperator.MSG) *= scala.math.log10(insertGapsWeight)

        // Calculate new values
        totalDensity = probability.values.sum
        println(s"total density: ${totalDensity}")
        probability.transform((_, value) => value/totalDensity)

        this.showProbabilities()
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
                mutant = BlockMutation.insertGap(parent)
            } else if (choice < probability(MutationOperator.EG)) {
                this.frequency(MutationOperator.EG)
                mutant = GapMutation.extendGap(parent)
            } else if (choice < probability(MutationOperator.RG)) {
                this.frequency(MutationOperator.RG) += 1
                mutant = GapMutation.removeGap(parent)
            } else if (choice < probability(MutationOperator.RSG)) {
                this.frequency(MutationOperator.RSG) += 1
                mutant = GapMutation.removeSingleGap(parent)
            } else if (choice < probability(MutationOperator.RGB)) {
                this.frequency(MutationOperator.RGB) += 1
                mutant = BlockMutation.removeGapBlock(parent)
            } else if (choice < probability(MutationOperator.TRG)) {
                this.frequency(MutationOperator.TRG) += 1
                mutant = BlockMutation.trimRedundantGaps(parent)
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

        mutant
    }


    /* Choose random mutation from all implemented, based on equal probability
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
                mutant = BlockMutation.insertGap(parent)
            } else if (choice == 2) {
                this.frequency(MutationOperator.EG) += 1
                mutant = GapMutation.extendGap(parent)
            } else if (choice == 3) {
                this.frequency(MutationOperator.RG) += 1
                mutant = GapMutation.removeGap(parent)
            } else if (choice == 4) {
                this.frequency(MutationOperator.RSG) += 1
                mutant = GapMutation.removeSingleGap(parent)
            } else if (choice == 5) {
                this.frequency(MutationOperator.RGB) += 1
                mutant = BlockMutation.removeGapBlock(parent)
            } else if (choice == 6) {
                this.frequency(MutationOperator.TRG) += 1
                mutant = BlockMutation.trimRedundantGaps(parent)
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

        mutant
    }


    def run(population: CurrentPopulation): CurrentPopulation = {
        val extendedPopulation: CurrentPopulation = population

        if (Config.isEarlyStage()) {
            for (_ <- 0 to Config.reproductionSize) extendedPopulation += Mutation.uniform(population)
        } else if (Config.isEndingStage()) {
            for (_ <- 0 to Config.reproductionSize) extendedPopulation += Mutation.stochastic(population)
        } else {
            for (_ <- 0 to Config.reproductionSize) extendedPopulation += Mutation.stochastic(population)
        }

        extendedPopulation
    }
}
