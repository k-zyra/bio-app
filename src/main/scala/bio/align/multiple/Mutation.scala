package bio.align.multiple

/* External imports */
import bio.align.multiple

import scala.util.Random

/* Internal imports */
import misc.Logger
import types.Biotype.{Alignment, CurrentPopulation}



object MutationOperator extends Enumeration {
    type MutationOperator = Value
    val UNDEF, ISG, IG, RG, RGB, TRG, MSG = Value
}

object Mutation {
    private val logger = new Logger("MSA_Mutation")

    var probabilities = Map(MutationOperator.ISG -> 0.1,
                            MutationOperator.IG -> 0.1,
                            MutationOperator.RG -> 0.1,
                            MutationOperator.RGB -> 0.1,
                            MutationOperator.TRG -> 0.1,
                            MutationOperator.MSG -> 0.1)


    def reEvaluteProbabilties(): Unit = {}


    /* Choose random mutation from all implemented, based on assigned probability
    */
    def stochastic(population: CurrentPopulation): Alignment = {
        var parentId: Int = Random.nextInt(population.size)
        var parent: Alignment = population(parentId)
        var mutant: Alignment = parent.clone()

        var usedMutation: MutationOperator.MutationOperator = MutationOperator.UNDEF
        var attempts: Int = 0
        while ((parent sameElements mutant)) {
            val choice: Double = Random.nextDouble()

            if (choice < 0.15) {
                usedMutation = MutationOperator.ISG
                mutant = GapMutation.insertSingleGap(parent)
            } else if (choice < 0.3) {
                usedMutation = MutationOperator.IG
                mutant = GapMutation.insertGap(parent)
            } else if (choice < 0.45) {
                usedMutation = MutationOperator.RG
                mutant = GapMutation.removeGap(parent)
            } else if (choice < 0.6) {
                usedMutation = MutationOperator.TRG
                mutant = GapMutation.trimRedundantGaps(parent)
            } else if (choice < 0.75) {
                usedMutation = MutationOperator.RGB
                mutant = GapMutation.removeGapBlock(parent)
            } else {
                usedMutation = MutationOperator.MSG
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
}
