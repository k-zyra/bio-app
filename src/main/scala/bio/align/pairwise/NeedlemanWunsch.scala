
package bio.align

/* External imports */
import java.nio.file.{Files, Path, Paths}
import java.util.Arrays
import org.apache.spark.sql.{DataFrame, Row}
import scala.annotation.switch
import scala.collection.mutable.{ArrayBuffer, ArrayBuilder, Map, StringBuilder}
import scala.xml.XML

/* Internal imports */
import app.SparkController
import bio.searchers.AlignSearcher
import misc.{Constants, Logger}


object NeedlemanWunsch {
    private var logger = new Logger("NeedlemanWunsch")



    /*  Get all alignments found using Needleman-Wunsch algorithm
    */
    def getNeedlemanWunschAlignments(sequences: Array[String],
                                            moves: Array[String]): Array[(String, String)] = {
        val firstSequence: String = sequences(0)
        val secondSequence: String = sequences(1)

        var row: Int = firstSequence.length
        var column: Int = secondSequence.length

        val leftShift: Int = 1
        val upShift: Int = secondSequence.length()
        val diagonalShift: Int = upShift + 1

        var firstAlignment: StringBuilder = new StringBuilder("")
        var secondAlignment: StringBuilder = new StringBuilder("")
        var alignments: ArrayBuffer[(String, String)] = new ArrayBuffer[(String, String)]()

        var nextMove: String = moves.last
        var nextMoveId: Int = moves.length - 1

        var run: Int = 0
        var step: Int = 0
        var maxNumberOfSteps: Int = row.max(column) - 1

        var keepReading: Boolean = true
        var toVisit: ArrayBuffer[(Int, Int, Char, Int)] = new ArrayBuffer()

        while (keepReading) {
            nextMove(0).toChar match {
                case '1' => {
                    nextMoveId = nextMoveId - diagonalShift
                    row -= 1
                    column -= 1
                    
                    firstAlignment.insert(0, firstSequence(row))
                    secondAlignment.insert(0, secondSequence(column))
                }
                case '2' => {
                    nextMoveId = nextMoveId - leftShift
                    column -= 1

                    firstAlignment.insert(0, '-')
                    secondAlignment.insert(0, secondSequence(column))
                }    
                case '3' => {
                    nextMoveId = nextMoveId - upShift
                    row -= 1

                    firstAlignment.insert(0, firstSequence(row))
                    secondAlignment.insert(0, '-')
                } 
            }
            if (step < maxNumberOfSteps) { // Sequences from the current path are not ready yet
                step += 1
                nextMove = moves(nextMoveId)

                if (nextMove.length > 1) {
                    for (possibleMove <- nextMove.substring(1)) toVisit += ((step, run, possibleMove, nextMoveId))
                }
            } else { // Sequences from the current path are ready
                val firstAl = firstAlignment.result()
                val secondAl = secondAlignment.result()

                alignments.+= (((firstAlignment.result(), secondAlignment.result())))
                firstAlignment.clear()
                secondAlignment.clear()

                if (toVisit.size == 0)  {
                    keepReading = false
                } else {
                    // newBranch = (step, run, possibleMove, nextMoveId)
                    var newBranch = toVisit.remove(0)

                    firstAlignment.append(alignments(newBranch._2)._1.takeRight(newBranch._1))
                    secondAlignment.append(alignments(newBranch._2)._2.takeRight(newBranch._1))
                    nextMove = newBranch._3.toString()
                    nextMoveId = newBranch._4

                    run += 1
                    step = newBranch._1

                    row = firstSequence.length - newBranch._1
                    column = secondSequence.length - newBranch._1
                }

            }
        }

        return alignments.toArray
    }


    /*  Find global alignment using Needleman-Wunsch algorithm
    */
    def needlemanWunschAlignment(sequences: Array[String],
                            substitutionMatrix: Array[Array[Int]],
                            reward: Integer = Constants.DefaultMatchReward,
                            gapPenalty: Integer = Constants.DefaultGapPenalty,
                            mismatchPenalty: Integer = Constants.DefaultMismatchPenalty, 
                            verbose: Boolean = logger.isVerbose()): Array[(String, String)] = {
        var alignments = Constants.EmptyAlignmentsArray
        var numberOfSequences = sequences.length 
        if (numberOfSequences != 2) {
            logger.logWarn(f"Incorrect number of sequences. Actual: ${numberOfSequences}, expected: 2")
            return alignments
        }

        val firstSequence = AlignSearcher.encodeSequence(sequences(0))
        val secondSequence = AlignSearcher.encodeSequence(sequences(1))

        val M: Int = firstSequence.length
        val N: Int = secondSequence.length

        var moves: ArrayBuffer[String] = new ArrayBuffer[String]()
        val temp  = ArrayBuffer.fill((M + Constants.ArrayPadding) * (N + Constants.ArrayPadding))(0)
        val helper: Array[Array[Integer]] = Array.ofDim[Integer](M + Constants.ArrayPadding, N + Constants.ArrayPadding)

        for (m <- 0 to M) helper(m)(0) = m * gapPenalty
        for (n <- 0 to N) helper(0)(n) = n * gapPenalty

        var id = N + 2
        val start: Long = System.nanoTime()
        for (m <- 1 to M) {
            for  (n <- 1 to N) {
                var alignmentsMap: Map[Int, Int] = Map[Int, Int]()
                var prev: Int = helper(m-1)(n-1)

                var alignValue: Int = prev + substitutionMatrix(firstSequence(m-1))(secondSequence(n-1))
                alignmentsMap += (Constants.Align -> alignValue)

                var upper: Int =  helper(m-1)(n)
                var verticalGap: Int = upper + gapPenalty
                alignmentsMap += (Constants.VerticalGap -> verticalGap)

                var left: Int = helper(m)(n-1)
                var horizontalGap: Int = left + gapPenalty
                alignmentsMap += (Constants.HorizontalGap -> horizontalGap)

                if (alignmentsMap.nonEmpty) {
                    val optimalValue = alignmentsMap.values.max
                    val optimalMoves = alignmentsMap.filter(_._2 == optimalValue).keys.toArray.mkString

                    helper(m)(n) = optimalValue
                    temp(id) = optimalValue 
                    moves += optimalMoves
                } else {
                    temp(id) = 0
                }
                id += 1
            }
            id += 1
        }

        alignments = this.getNeedlemanWunschAlignments(sequences, moves.toArray)
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) {
            logger.logInfo(f"Alignments: (${alignments.size}) using Needleman-Wunsch algorithm collected in ${duration} ms")
        }
        return alignments
    }

    
    /*  Find global alignment using Needleman-Wunsch algorithm with affine gap penalty
    */
    def needlemanWunschAlignmentAffine(sequences: Array[String],
                            substitutionMatrix: Array[Array[Int]],
                            reward: Integer = Constants.DefaultMatchReward,
                            gapPenalty: Integer = Constants.DefaultGapPenalty,
                            gapExtensionPenalty: Integer = Constants.DefaultGapExtensionPenalty,
                            mismatchPenalty: Integer = Constants.DefaultMismatchPenalty, 
                            verbose: Boolean = logger.isVerbose()): Array[String] = {
        var matches = Constants.EmptyStringArray
        var numberOfSequences = sequences.length 
        if (numberOfSequences != 2) {
            logger.logWarn(f"Incorrect number of sequences. Actual:${numberOfSequences}, expected: 2")
            return matches
        }

        val firstSequence = AlignSearcher.encodeSequence(sequences(0))
        val secondSequence = AlignSearcher.encodeSequence(sequences(1))

        val M: Int = firstSequence.length
        val N: Int = secondSequence.length

        val moves: Array[Array[Int]] = Array.ofDim[Int]((M + Constants.ArrayPadding) * (N + Constants.ArrayPadding), 3)
        val temp  = ArrayBuffer.fill((M + Constants.ArrayPadding) * (N + Constants.ArrayPadding))(0)
        val helper: Array[Array[Int]] = Array.ofDim[Int](M + Constants.ArrayPadding, N + Constants.ArrayPadding)

        for (m <- 0 to M) helper(m)(0) = m * gapPenalty
        for (n <- 0 to N) helper(0)(n) = n * gapPenalty

        var id = N + 2
        var gapLength: Integer = 0
        val start: Long = System.nanoTime()
        for (m <- 1 to M) {
            for  (n <- 1 to N) {
                var alignmentsMap: Map[Int, Int] = Map[Int, Int]()
                var prev: Int = helper(m-1)(n-1)

                var upper: Int = helper(m-1)(n)
                var horizontalGap: Int = upper + (gapPenalty - gapLength * gapExtensionPenalty)
                alignmentsMap += (Constants.HorizontalGap -> horizontalGap)

                var left: Int = helper(m)(n-1)
                var verticalGap: Int = left + (gapPenalty - gapLength * gapExtensionPenalty)
                alignmentsMap += (Constants.VerticalGap -> verticalGap)

                var alignValue: Int = prev + substitutionMatrix(firstSequence(m-1))(secondSequence(n-1))
                alignmentsMap += (Constants.Align -> alignValue)

                if (alignValue < horizontalGap || alignValue < verticalGap) {
                    gapLength += 1
                } else {
                    gapLength = 0
                }

                if (alignmentsMap.nonEmpty) {
                    val optimalValue = alignmentsMap.values.max
                    val optimalMoves = alignmentsMap.filter(_._2 == optimalValue).keys.toArray

                    helper(m)(n) = optimalValue
                    temp(id) = optimalValue 
                    moves(id) = optimalMoves 
                } else {
                    temp(id) = 0 
                }
                id += 1
            }
            id += 1
        }

        val finalMatrix = temp.result()
        val maxScore: Int = finalMatrix.max
        val indexes: Array[Int] = finalMatrix.zipWithIndex.filter { case (value, _) => value == maxScore }.map(_._2).toArray

        val arrayOfPairsBuffer = ArrayBuffer[(Int, Int)]()
        for (i <- 0 to helper.size-1) {
            val alter = helper(i).zipWithIndex.filter { case (value, _) => value == maxScore }.map(_._2).toArray
            arrayOfPairsBuffer ++= alter.map(value => (i, value))
        }
        val result = arrayOfPairsBuffer.result().toArray
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) {
            logger.logInfo(f"Alignments (${result.size}) using Needleman-Wunsch algorithm collected in $duration ms")
        }
        return matches
    }


}
