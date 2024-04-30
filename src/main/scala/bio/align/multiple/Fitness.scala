package bio.align.multiple

/* External imports */
import com.github.vickumar1981.stringdistance.StringDistance.{Hamming, Jaccard, Jaro, Levenshtein}

import scala.collection.mutable.ArrayBuffer

/* Internal imports */
import misc.Constants
import types.Biotype.{Alignment, CurrentPopulation}
import types.DistanceType.DistanceType
import types.{DistanceType, ScoringType}
import types.ScoringType.ScoringType


object Fitness {


    /* Calculate distance between two sequences
    */
    def getDistance(firstSequence: String,
                    secondSequence: String,
                    distType: DistanceType = DistanceType.LEVENSHTEIN): Double = {
        var distance: Double = 0
        distType match {
            case DistanceType.LEVENSHTEIN => distance = Levenshtein.score(firstSequence, secondSequence)
            case DistanceType.HAMMING => distance = Hamming.score(firstSequence, secondSequence)
            case DistanceType.JACCARD => distance = Jaccard.score(firstSequence, secondSequence)
            case DistanceType.JARO => distance = Jaro.score(firstSequence, secondSequence)
        }

        distance
    }


    /* Calculate average distance from one sequence to all other sequences in array
    */
    def getAverageDistance(referenceSequence: String,
                           seqeunces: Array[String],
                           distType: DistanceType = DistanceType.LEVENSHTEIN): Double = {
        val distanceSum = seqeunces.map(this.getDistance(referenceSequence, _, distType)).sum
        distanceSum/seqeunces.length
    }


    /* Calculate pair cost based on values from chosen matrix
    */
    def getPairCost(firstSequence: String,
                    secondSequence: String,
                    scoringType: ScoringType = Config.defaultScoring): Int = {
        var matrix: Map[String, Int] = Map[String, Int]()
        scoringType match {
            case ScoringType.PAM250 => matrix = scoring.Pam.Pam250
            case ScoringType.BLOSUM62 => matrix = scoring.Blosum.Blosum62
            case ScoringType.SUBSTITUTION => matrix = scoring.SubstitutionMatrix.DnaDefaultMatrix
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

        cost
    }


    /* Get total cost for given alignment
    */
    def getAlignmentCost(alignment: Alignment): Int = {
        val ref = alignment.head
        var totalCost: Int = 0

        for (i <- 1 to alignment.length - Constants.ArrayPadding) totalCost += this.getPairCost(ref, alignment(i))
        totalCost
    }


    /* Rank alignments to choose the best species to be included in the next generation
    */
    def rankAlignments(alignments: CurrentPopulation): Array[(Int, Int)] = {
        val costs = alignments.map(alignment => Fitness.getAlignmentCost(alignment)).zipWithIndex
        costs.sortBy(-_._1).toArray
    }


    /* Choose the best part of current population as a base of the next population
    */
    def getFittestSpecies(alignments: CurrentPopulation,
                          numberOfSpecies: Int): CurrentPopulation = {
        val populationBase: ArrayBuffer[Alignment] = new ArrayBuffer[Alignment]()
        val rankedSpecies: Array[(Int, Int)] = this.rankAlignments(alignments)

        for (specimen <- rankedSpecies.take(numberOfSpecies)) {
            populationBase += alignments(specimen._2)
        }

        populationBase
    }


    /* Select more promising child for further breeding
    */
    def chooseChild(firstChild: Alignment,
                            secondChild: Alignment): Alignment = {
        val firstChildScore: Int = Fitness.getAlignmentCost(firstChild)
        val secondChildScore: Int = Fitness.getAlignmentCost(secondChild)

        if (firstChildScore > secondChildScore) firstChild
        else secondChild
    }
}
