package bio.searchers

/* External imports */
import java.nio.file.{Files, Path, Paths}
import java.util.Arrays

import org.apache.spark.sql.{DataFrame, Row}

import scala.annotation.switch
import scala.collection.mutable.{ArrayBuffer, ArrayBuilder, Map, StringBuilder}
import scala.xml.XML

/* Internal imports */
import app.SparkController

import utils.{Constants, Logger}



object AlignSearcher {
    private var logger = new Logger("AlignSearcher")

    /* Prepare score matrix 
     * Read XML file and prepare score matrix
    */
     def prepareSubstitutionMatrix(filename: String = Constants.ExampleSubstitutionMatrix): Array[Array[Int]] = {
        val filePath: Path = Paths.get(Constants.DataDir + "\\" + filename)
        if (Files.notExists(filePath)) {
            logger.logError(f"File ${filePath} does not exist. Cannot prepare substitution matrix.")
        } 

        val xml = XML.loadFile(filePath.toString())
        val substitutionMatrix: Array[Array[Int]] = (xml \ "row").map { row => (row \ "column").map(_.text.toInt).toArray}.toArray

        return substitutionMatrix
    }


    /*  Display score matrix in readible format
    */
    def displaySubstitutionMatrix(matrix: Array[Array[Int]], 
                        rows: Array[String] = Constants.EmptyStringArray,
                        columns: Array[String] = Constants.EmptyStringArray): Unit = {
        val rows: Integer = matrix.size
        val columns: Integer = matrix(0).size

        for (i <- 0 until rows) {
			for (j <- 0 until columns) {
				print(matrix(i)(j) + " ")
			}
			println("")
		}
    }


    /*  Display alignment matrix in readible format
    */
    def displayAlignmentMatrix(matrix: Array[Array[Integer]],
                            rows: Array[String] = Constants.EmptyStringArray,
                            columns: Array[String] = Constants.EmptyStringArray): Unit = {
        val rows: Integer = matrix.size
        val columns: Integer = matrix(0).size

        for (i <- 0 until rows) {
			for (j <- 0 until columns) {
				print(matrix(i)(j) + " ")
			}
			println("")
		}
    }


    /* Check size of given substitution matrix
    */
    private def isSubstitutionMatrixCorrectSize(sequences: Array[String], 
                                        substitutionMatrix: Array[Array[Int]]): Boolean = {
        val rows: Integer = substitutionMatrix.length
        val columns: Integer = substitutionMatrix(0).length
        if (rows != columns) {
            logger.logWarn(f"Number of rows and columns in score matrix should be equal.")
            return false
        }

        val sequencesPar = sequences.par
        val res = sequencesPar.map(str => str.groupBy(c => c.toLower).map(e => (e._1, e._2.length)).toList.length)
        val requiredSize = res.toArray.max
        
        if (rows != requiredSize) {
            logger.logWarn(f"Invalid size of score matrix. Actual: ${rows}x${columns}, expected: ${requiredSize}x${requiredSize}")
            return false
        }

        return true
    }


    /*  Encode seqeuence by converting string to an array of integers
    */
    private def encodeSequence(sequence: String): Array[Integer] = {
        var encoded = new ArrayBuilder.ofRef[Integer]

        for (base <- sequence) {
            base match {
                case 'A' => encoded += 0
                case 'C' => encoded += 1
                case 'G' => encoded += 2
                case 'T' => encoded += 3
            }
        }        

        return encoded.result()
    }


    /*  Find local alignment using Smith-Waterman algorithm
    */
    def smithWatermanAlignment(sequences: Array[String],
                            substitutionMatrix: Array[Array[Int]],
                            penalty: Integer = Constants.DefaultGapPenalty,
                            verbose: Boolean = logger.isVerbose()): Array[String] = {
        var matches = Constants.EmptyStringArray
        var numberOfSequences = sequences.length 
        if (numberOfSequences != 2) {
            logger.logWarn(f"Incorrect number of sequences. Actual:$numberOfSequences, expected: 2")
            return matches
        }

        val firstSequence = this.encodeSequence(sequences(0))
        val secondSequence = this.encodeSequence(sequences(1))

        val M: Integer = firstSequence.length
        val N: Integer = secondSequence.length

        val moves: Array[Array[Integer]] = Array.ofDim[Integer]((M + Constants.ArrayPadding) * (N + Constants.ArrayPadding), 3)
        val temp  = ArrayBuffer.fill((M + Constants.ArrayPadding) * (N + Constants.ArrayPadding))(0)
        val helper: Array[Array[Integer]] = Array.ofDim[Integer](M + Constants.ArrayPadding, N + Constants.ArrayPadding)

        for (m <- 0 to M) helper(m)(0) = 0
        for (n <- 0 to N) helper(0)(n) = 0

        var id = N + 2
        val start: Long = System.nanoTime()
        for (m <- 1 to M) {
            for  (n <- 1 to N) {
                var alignmentsMap: Map[Integer, Integer] = Map[Integer, Integer]()
                var prev: Integer = helper(m-1)(n-1)

                var alignValue: Integer = prev + substitutionMatrix(firstSequence(m-1))(secondSequence(n-1))
                if (alignValue >= 0) {
                    alignmentsMap += (Constants.Align -> alignValue)
                }

                var upper: Integer =  helper(m-1)(n)
                var horizontalGap: Integer = upper + penalty
                if (horizontalGap >= 0) {
                    alignmentsMap += (Constants.HorizontalGap -> horizontalGap)
                }

                var left: Integer = helper(m)(n-1)
                var verticalGap: Integer = left + penalty
                if (verticalGap >= 0) {
                    alignmentsMap += (Constants.VerticalGap -> verticalGap)
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

        var finalMatrix = temp.result()
        var maxScore: Integer = finalMatrix.max
        val indexes: Array[Int] = finalMatrix.zipWithIndex.filter { case (value, _) => value == maxScore }.map(_._2).toArray

        val arrayOfPairsBuffer = ArrayBuffer[(Int, Int)]()
        for (i <- 0 to helper.size-1) {
            val alter = helper(i).zipWithIndex.filter { case (value, _) => value == maxScore }.map(_._2).toArray
            arrayOfPairsBuffer ++= alter.map(value => (i, value))
        }

        val result = arrayOfPairsBuffer.result().toArray

        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis
        if (verbose) {
            logger.logInfo(f"Matches found: ${result.size}")
            logger.logInfo(f"Alignments using Smith-Waterman algorithm collected in $duration ms")
        }

        return matches
    }


    /*  Get all alignments found using Needleman-Wunsch algorithm
    */
    private def getNeedlemanWunschAlignments(sequences: Array[String],
                                            matrix: Array[Array[Integer]],
                                            moves: Array[String]): Array[(String, String)] = {
        this.displayAlignmentMatrix(matrix)

        var row: Int = matrix.length - 1
        var column: Int = matrix(0).length - 1

        val firstSequence: String = sequences(0)
        val secondSequence: String = sequences(1)

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
            if (nextMoveId > 0) { // Sequences from the current run are not ready yet
                step += 1
                nextMove = moves(nextMoveId)

                if (nextMove.length > 1) {
                    for (possibleMove <- nextMove.substring(1)) toVisit += ((step, run, possibleMove, nextMoveId))
                }
            } else { // Sequences from the current path are ready
                firstAlignment.insert(0, firstSequence(0))
                secondAlignment.insert(0, secondSequence(0))

                val firstAl = firstAlignment.result()
                val secondAl = secondAlignment.result()

                alignments.+= (((firstAlignment.result(), secondAlignment.result())))
                firstAlignment.clear()
                secondAlignment.clear()

                if (toVisit.size == 0)  {
                    keepReading = false
                } else {
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
        var matches = Constants.EmptyMatchesArray
        var numberOfSequences = sequences.length 
        if (numberOfSequences != 2) {
            logger.logWarn(f"Incorrect number of sequences. Actual: ${numberOfSequences}, expected: 2")
            return matches
        }

        val firstSequence = this.encodeSequence(sequences(0))
        val secondSequence = this.encodeSequence(sequences(1))

        val M: Integer = firstSequence.length
        val N: Integer = secondSequence.length

        var moves: ArrayBuffer[String] = new ArrayBuffer[String]()
        val temp  = ArrayBuffer.fill((M + Constants.ArrayPadding) * (N + Constants.ArrayPadding))(0)
        val helper: Array[Array[Integer]] = Array.ofDim[Integer](M + Constants.ArrayPadding, N + Constants.ArrayPadding)

        for (m <- 0 to M) helper(m)(0) = m * gapPenalty
        for (n <- 0 to N) helper(0)(n) = n * gapPenalty

        var id = N + 2
        val start: Long = System.nanoTime()
        for (m <- 1 to M) {
            for  (n <- 1 to N) {
                var alignmentsMap: Map[Integer, Integer] = Map[Integer, Integer]()
                var prev: Integer = helper(m-1)(n-1)

                var alignValue: Integer = prev + substitutionMatrix(firstSequence(m-1))(secondSequence(n-1))
                alignmentsMap += (Constants.Align -> alignValue)

                var upper: Integer =  helper(m-1)(n)
                var verticalGap: Integer = upper + gapPenalty
                alignmentsMap += (Constants.VerticalGap -> verticalGap)

                var left: Integer = helper(m)(n-1)
                var horizontalGap: Integer = left + gapPenalty
                alignmentsMap += (Constants.HorizontalGap -> horizontalGap)

                if (alignmentsMap.nonEmpty) {
                    val optimalValue = alignmentsMap.values.max
                    val optimalMoves = alignmentsMap.filter(_._2 == optimalValue).keys.toArray
                    val optimalStr = alignmentsMap.filter(_._2 == optimalValue).keys.toArray.mkString

                    helper(m)(n) = optimalValue
                    temp(id) = optimalValue 
                    moves += optimalStr
                } else {
                    temp(id) = 0
                }
                id += 1
            }
            id += 1
        }

        matches = this.getNeedlemanWunschAlignments(sequences, helper, moves.toArray)
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) {
            logger.logInfo(f"Alignments: ${matches.size} using Needleman-Wunsch algorithm collected in $duration ms")
        }
        return matches
    }

    
    /*  Find global alignment using Needleman-Wunsch algorithm
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
            logger.logWarn(f"Incorrect number of sequences. Actual:$numberOfSequences, expected: 2")
            return matches
        }

        val firstSequence = this.encodeSequence(sequences(0))
        val secondSequence = this.encodeSequence(sequences(1))

        val M: Integer = firstSequence.length
        val N: Integer = secondSequence.length

        val moves: Array[Array[Integer]] = Array.ofDim[Integer]((M + Constants.ArrayPadding) * (N + Constants.ArrayPadding), 3)
        val temp  = ArrayBuffer.fill((M + Constants.ArrayPadding) * (N + Constants.ArrayPadding))(0)
        val helper: Array[Array[Integer]] = Array.ofDim[Integer](M + Constants.ArrayPadding, N + Constants.ArrayPadding)

        for (m <- 0 to M) helper(m)(0) = m * gapPenalty
        for (n <- 0 to N) helper(0)(n) = n * gapPenalty

        var id = N + 2
        var gapLength: Integer = 0
        val start: Long = System.nanoTime()
        for (m <- 1 to M) {
            for  (n <- 1 to N) {
                var alignmentsMap: Map[Integer, Integer] = Map[Integer, Integer]()
                var prev: Integer = helper(m-1)(n-1)

                var upper: Integer = helper(m-1)(n)
                var horizontalGap: Integer = upper + (gapPenalty - gapLength * gapExtensionPenalty)
                alignmentsMap += (Constants.HorizontalGap -> horizontalGap)

                var left: Integer = helper(m)(n-1)
                var verticalGap: Integer = left + (gapPenalty - gapLength * gapExtensionPenalty)
                alignmentsMap += (Constants.VerticalGap -> verticalGap)

                var alignValue: Integer = prev + substitutionMatrix(firstSequence(m-1))(secondSequence(n-1))
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

        var finalMatrix = temp.result()
        var maxScore: Integer = finalMatrix.max
        val indexes: Array[Int] = finalMatrix.zipWithIndex.filter { case (value, _) => value == maxScore }.map(_._2).toArray

        val arrayOfPairsBuffer = ArrayBuffer[(Int, Int)]()
        for (i <- 0 to helper.size-1) {
            val alter = helper(i).zipWithIndex.filter { case (value, _) => value == maxScore }.map(_._2).toArray
            arrayOfPairsBuffer ++= alter.map(value => (i, value))
        }

        val result = arrayOfPairsBuffer.result().toArray

        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis
        if (verbose) {
            logger.logInfo(f"Matches found: ${result.size}")
            logger.logInfo(f"Alignments using Needleman-Wunsch algorithm collected in $duration ms")
        }

        this.displayAlignmentMatrix(helper)
        return matches
    }


    /*  Find global alignment using Needleman-Wunsch algorithm
    */
    def evolutionaryAlgorithmAlignment(): Unit = {

    }
}
