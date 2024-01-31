package bio.searchers

/* External imports */
import java.nio.file.{Files, Path, Paths}
import java.util.Arrays

import org.apache.spark.sql.{DataFrame, Row}

import scala.annotation.switch
import scala.collection.mutable
import scala.collection.mutable.ArrayBuilder
import scala.collection.mutable.StringBuilder
import scala.collection.mutable.Map
import scala.xml.XML

/* Internal imports */
import app.SparkController

import utils.Constants
import utils.Logger



object AlignSearcher {
    private var logger = new Logger("AlignSearcher")

    /* Prepare score matrix 
     * Read XML file and prepare score matrix
    */
     def prepareScoreMatrix(filename: String = Constants.EXAMPLE_SCORE_MATRIX): Array[Array[Int]] = {
        val filePath: Path = Paths.get(Constants.DATA_DIR + "\\" + filename)
        if (Files.notExists(filePath)) {
            logger.logError(f"File ${filePath} does not exist. Cannot prepare score matrix.")
        } 

        val xml = XML.loadFile(filePath.toString())
        val scoreMatrix: Array[Array[Int]] = (xml \ "row").map { row => (row \ "column").map(_.text.toInt).toArray}.toArray

        return scoreMatrix
    }


    /*  Display score matrix in readible format
    */
    def displayScoreMatrix(matrix: Array[Array[Int]], 
                        rows: Array[String] = Constants.EMPTY_STRING_ARRAY,
                        columns: Array[String] = Constants.EMPTY_STRING_ARRAY): Unit = {
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
                            rows: Array[String] = Constants.EMPTY_STRING_ARRAY,
                            columns: Array[String] = Constants.EMPTY_STRING_ARRAY): Unit = {
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
    private def isScoreMatrixCorrectSize(sequences: Array[String], 
                                        scoreMatrix: Array[Array[Int]]): Boolean = {
        val rows: Integer = scoreMatrix.length
        val columns: Integer = scoreMatrix(0).length
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
        var encoded = new mutable.ArrayBuilder.ofRef[Integer]

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
    def smithWatermanSearch(sequences: Array[String],
                            scoreMatrix: Array[Array[Int]],
                            penalty: Integer = Constants.DEFAULT_GAP_PENALTY,
                            verbose: Boolean = logger.isVerbose()): Int = {
                            // verbose: Boolean = logger.isVerbose()): Array[String] = {
        var matches = Constants.EMPTY_STRING_ARRAY
        var numberOfSequences = sequences.length 
        if (numberOfSequences != 2) {
            logger.logWarn(f"Incorrect number of sequences. Actual:$numberOfSequences, expected: 2")
            return 0
            // return matches
        }

        val firstSequence = this.encodeSequence(sequences(0))
        val secondSequence = this.encodeSequence(sequences(1))

        val M: Integer = firstSequence.length
        val N: Integer = secondSequence.length

        val moves: Array[Array[Integer]] = Array.ofDim[Integer]((M + Constants.ARRAY_PADDING) * (N + Constants.ARRAY_PADDING), 3)
        val temp  = mutable.ArrayBuffer.fill((M + Constants.ARRAY_PADDING) * (N + Constants.ARRAY_PADDING))(0)
        val helper: Array[Array[Integer]] = Array.ofDim[Integer](M + Constants.ARRAY_PADDING, N + Constants.ARRAY_PADDING)

        for (m <- 0 to M) helper(m)(0) = 0
        for (n <- 0 to N) helper(0)(n) = 0

        var id = N + 2
        var start = System.currentTimeMillis()
        for (m <- 1 to M) {
            for  (n <- 1 to N) {
                var alignmentsMap: mutable.Map[Integer, Integer] = mutable.Map[Integer, Integer]()
                var prev: Integer = helper(m-1)(n-1)

                var alignValue: Integer = prev + scoreMatrix(firstSequence(m-1))(secondSequence(n-1))
                if (alignValue >= 0) {
                    alignmentsMap += (Constants.ALIGN -> alignValue)
                }

                var upper: Integer =  helper(m-1)(n)
                var horizontalGap: Integer = upper + penalty
                if (horizontalGap >= 0) {
                    alignmentsMap += (Constants.HORIZONTAL_GAP -> horizontalGap)
                }

                var left: Integer = helper(m)(n-1)
                var verticalGap: Integer = left + penalty
                if (verticalGap >= 0) {
                    alignmentsMap += (Constants.VERTiCAL_GAP -> verticalGap)
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

        val arrayOfPairsBuffer = mutable.ArrayBuffer[(Int, Int)]()
        for (i <- 0 to helper.size-1) {
            val alter = helper(i).zipWithIndex.filter { case (value, _) => value == maxScore }.map(_._2).toArray
            arrayOfPairsBuffer ++= alter.map(value => (i, value))
        }

        val result = arrayOfPairsBuffer.result().toArray

        val duration = System.currentTimeMillis() - start
        if (verbose) {
            logger.logInfo(f"Matches found: ${result.size}")
            logger.logInfo(f"Alignments using Smith-Waterman method found in $duration ms")
        }

        // return matches
        return result.size
    }


    /*  Find global alignment using Needleman-Wunsch algorithm
    */
    def needlemanWunschSearch(sequences: Array[String],
                            reward: Integer = Constants.DEFAULT_MATCH_REWARD,
                            penalty: Integer = Constants.DEFAULT_GAP_PENALTY, 
                            verbose: Boolean = logger.isVerbose()): Unit = {

    }


    /*  Find global alignment using Needleman-Wunsch algorithm with affine gap penalty
    */
    def affineNeedlemanWunschSearch(sequences: Array[String]): Unit = {
        
    }
}
