
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


object SmithWaterman {
    private var logger = new Logger("SmithWaterman")


    /*  Get all alignments found using Smith-Waterman algorithm
    */
    def getSmithWatermanAlignments(sequences: Array[String],
                                endingPoints: Array[(Int, Int)],
                                moves: Array[String]): Array[(String, String)] = {
        var alignments: ArrayBuffer[(String, String)] = new ArrayBuffer[(String, String)]

        val firstSequence: String = sequences(0)
        val secondSequence: String = sequences(1)

        var numOfRows: Int = firstSequence.length() + 1
        var numOfColumns: Int = secondSequence.length() + 1

        var firstAlignment: StringBuilder = new StringBuilder(Constants.EmptyString)
        var secondAlignment: StringBuilder = new StringBuilder(Constants.EmptyString)

        val leftShift: Int = 1
        val upShift: Int = numOfColumns
        val diagonalShift: Int = numOfColumns + 1

        for (point <- endingPoints) {
            var row: Int = point._1 - 1
            var column: Int = point._2 - 1

            var nextMoveId: Int = column + numOfColumns * row 
            var nextMove: String = moves(nextMoveId)

            var keepReading: Boolean = true
            while(keepReading && nextMove != null) {
                nextMove(0).toChar match {
                    case '1' => {
                        firstAlignment.insert(0, firstSequence(row))
                        secondAlignment.insert(0, secondSequence(column))

                        nextMoveId = nextMoveId - diagonalShift
                        row -= 1
                        column -= 1                        
                    }
                    case '2' => {
                        firstAlignment.insert(0, '-')
                        secondAlignment.insert(0, secondSequence(column))

                        nextMoveId = nextMoveId - leftShift
                        column -= 1
                    }    
                    case '3' => {
                        firstAlignment.insert(0, firstSequence(row))
                        secondAlignment.insert(0, '-')

                        nextMoveId = nextMoveId - upShift
                        row -= 1
                    }
                }

                if (row < 0 || column < 0) {
                    keepReading = false
                } else {
                    nextMove = moves(nextMoveId)
                    if (nextMove == null) {
                        keepReading = false
                        firstAlignment.insert(0, firstSequence(row))
                        secondAlignment.insert(0, secondSequence(column))
                    }
                }
            }
            alignments.+= (((firstAlignment.result(), secondAlignment.result())))
            firstAlignment.clear()
            secondAlignment.clear()
        }

        return alignments.toArray
    }


    /*  Find local alignment using Smith-Waterman algorithm
    */
    def smithWatermanAlignment(sequences: Array[String],
                            substitutionMatrix: Array[Array[Int]],
                            penalty: Integer = Constants.DefaultGapPenalty,
                            verbose: Boolean = logger.isVerbose()): Array[(String, String)] = {
        var alignments = Constants.EmptyAlignmentsArray
        val numberOfSequences = sequences.length 
        if (numberOfSequences != 2) {
            logger.logWarn(f"Incorrect number of sequences. Actual:$numberOfSequences, expected: 2")
            return alignments
        }

        val rows: Int = sequences(0).length + 1
        val columns: Int = sequences(1).length + 1

        val firstSequence = AlignSearcher.encodeSequence(sequences(0))
        val secondSequence = AlignSearcher.encodeSequence(sequences(1))

        val M: Int = firstSequence.length
        val N: Int = secondSequence.length

        var moves: Array[String] = Array.ofDim[String](rows * columns)
        val temp  = ArrayBuffer.fill((M + Constants.ArrayPadding) * (N + Constants.ArrayPadding))(0)
        val helper: Array[Array[Int]] = Array.ofDim[Int](M + Constants.ArrayPadding, N + Constants.ArrayPadding)

        for (m <- 0 to M) helper(m)(0) = 0
        for (n <- 0 to N) helper(0)(n) = 0

        var id = N + 2
        val start: Long = System.nanoTime()
        for (m <- 1 to M) {
            for  (n <- 1 to N) {
                var alignmentsMap: Map[Int, Int] = Map[Int, Int]()
                var prev: Int = helper(m-1)(n-1)

                var alignValue: Int = prev + substitutionMatrix(firstSequence(m-1))(secondSequence(n-1))
                if (alignValue >= 0) {
                    alignmentsMap += (Constants.Align -> alignValue)
                }

                var upper: Int = helper(m-1)(n)
                var verticalGap: Int = upper + penalty
                if (verticalGap >= 0) {
                    alignmentsMap += (Constants.VerticalGap -> verticalGap)
                }

                var left: Int = helper(m)(n-1)
                var horizontalGap: Int = left + penalty
                if (horizontalGap >= 0) {
                    alignmentsMap += (Constants.HorizontalGap -> horizontalGap)
                }

                if (alignmentsMap.nonEmpty) {
                    val optimalValue = alignmentsMap.values.max
                    val optimalMoves = alignmentsMap.filter(_._2 == optimalValue).keys.toArray.mkString

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

        val maxScore: Int = temp.result().max
        val arrayOfPairsBuffer = ArrayBuffer[(Int, Int)]()
        for (i <- 0 to helper.size-1) {
            val alter = helper(i).zipWithIndex.filter { case (value, _) => value == maxScore }.map(_._2).toArray
            arrayOfPairsBuffer ++= alter.map(value => (i, value))
        }
        alignments = this.getSmithWatermanAlignments(sequences, arrayOfPairsBuffer.toArray, moves.toArray)
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) {
            logger.logInfo(f"Alignments (${alignments.size}) using Smith-Waterman algorithm collected in ${duration} ms")
        }
        return alignments
    }


}
