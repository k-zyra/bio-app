package bio.align.multiple

import com.github.vickumar1981.stringdistance.StringDistance.{Hamming, Jaccard, Jaro, Levenshtein}
import misc.{Constants, Logger}
import types.Biotype.{Alignment, CurrentPopulation}
import types.DistanceType.DistanceType
import types.{DistanceType, ScoringType}
import types.ScoringType.ScoringType

import scala.collection.mutable.ArrayBuffer

object Fitness {
    private val logger = new Logger("MSA_Fitness")



    /* Calculate distance between two sequences
    */
    def getDistance(firstSequence: String,
                    secondSequence: String,
                    distType: DistanceType = DistanceType.HAMMING): Double = {
        var distance: Double = 0
        distType match {
            case DistanceType.HAMMING => distance = Hamming.score(firstSequence, secondSequence)
            case DistanceType.JACCARD => distance = Jaccard.score(firstSequence, secondSequence)
            case DistanceType.JARO => distance = Jaro.score(firstSequence, secondSequence)
            case DistanceType.LEVENSHTEIN => distance = Levenshtein.score(firstSequence, secondSequence)
        }

        return distance
    }


    /* Calculate pair cost based on values from chosen matrix
    */
    def getPairCost(firstSequence: String,
                    secondSequence: String,
                    scoringType: ScoringType = ScoringType.PAM250): Int = {
        var matrix: Map[String, Int] = Map[String, Int]()
        scoringType match {
            case ScoringType.PAM250 => matrix = scoring.Pam.Pam250
            case ScoringType.BLOSUM62 => matrix = scoring.Blosum.Blosum62
        }

        var cost: Int = 0
        var gapLen: Int = 0
        var gapCost: Int = 0
        for (i <- 0 to firstSequence.length - Constants.ArrayPadding) {
            if (firstSequence(i) == '-' || secondSequence(i) == '-') {
                if (gapLen == 0) gapCost += scoring.Pam.gapOpening
                else gapCost += scoring.Pam.gapExtension
                gapLen += 1
            } else {
                cost += (matrix.getOrElse(s"${firstSequence(i)}${secondSequence(i)}", 0) + gapCost)
                gapCost = 0
                gapLen = 0
            }
        }

        return cost
    }


    /* Get total cost for given alignment
    */
    def getAlignmentCost(alignment: Alignment): Int = {
        val ref = alignment.head
        var totalCost: Int = 0

        for (i <- 1 to alignment.length - Constants.ArrayPadding) totalCost += this.getPairCost(ref, alignment(i))
        return totalCost
    }


    /* Rank alignments to choose the best species to be included in the next generation
    */
    def rankAlignments(alignments: CurrentPopulation,
                       verbose: Boolean = logger.isVerbose()): Array[(Int, Int)] = {
        val start: Long = System.nanoTime()
        val costs = alignments.map(alignment => Fitness.getAlignmentCost(alignment)).zipWithIndex
        val rankedCosts = costs.sortBy(-_._1)
        val duration: Float = (System.nanoTime() - start) / Constants.NanoInMillis

        //        if (verbose) logger.logInfo(f"Time spent in <rankAlignments> ${duration} ms")
        return rankedCosts.toArray
    }


    /* Choose the best part of current population as a base of the next population
    */
    def getFittestSpecies(alignments: CurrentPopulation,
                                  numberOfSpecies: Int,
                                  verbose: Boolean = logger.isVerbose()): CurrentPopulation = {
        val populationBase: ArrayBuffer[Alignment] = new ArrayBuffer[Alignment]()
        val rankedSpecies: Array[(Int, Int)] = this.rankAlignments(alignments)
        //        if (verbose) logger.logInfo(s"Current best score: ${rankedSpecies.head._1}: ")

        for (specimen <- rankedSpecies.take(numberOfSpecies)) {
            populationBase += alignments(specimen._2)
        }

        //        if (verbose) logger.logInfo(s"Get ${this.replacementSize} best species as a base for the next population.")
        return populationBase
    }


    /* Select more promising child for further breeding
    */
    def chooseChild(firstChild: Alignment,
                            secondChild: Alignment,
                            verbose: Boolean = logger.isVerbose()): Alignment = {
        val firstChildScore: Int = Fitness.getAlignmentCost(firstChild)
        val secondChildScore: Int = Fitness.getAlignmentCost(secondChild)

        //        if (verbose) println(s"[INFO] First=${firstChildScore}, Second=${secondChildScore}")
        if (firstChildScore > secondChildScore) return firstChild
        else return secondChild
    }
}
