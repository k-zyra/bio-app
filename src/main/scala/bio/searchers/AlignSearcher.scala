package bio.searchers

/* External imports */
import java.nio.file.{Files, Path, Paths}
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, Map, StringBuilder}
import scala.language.implicitConversions
import scala.xml.XML

/* Internal imports */
import misc.{Constants, Logger}


object AlignSearcher {
    private val logger = new Logger("AlignSearcher")


    implicit def convertToArray(arrayBuffer: ArrayBuffer[String]): Array[String] = {
        arrayBuffer.result().toArray
    }


    implicit def convertToString(stringBuilder: StringBuilder): String = {
        stringBuilder.result()
    }


    /* Prepare score matrix 
     * Read XML file and prepare score matrix
    */
     def prepareSubstitutionMatrix(filename: String): Array[Array[Int]] = {
        val filePath: Path = Paths.get(Constants.DataDir + "\\" + filename)
        if (Files.notExists(filePath)) {
            logger.logError(f"File ${filePath} does not exist. Cannot prepare substitution matrix.")
        } 

        val xml = XML.loadFile(filePath.toString())
        val substitutionMatrix: Array[Array[Int]] = (xml \ "row").map { row => (row \ "column").map(_.text.toInt).toArray}.toArray

        substitutionMatrix
    }


    /*  Display pair of alignments in readible format
    */
    def displayAlignments(sequences: (String, String)): Unit = {
        println(f"(${sequences._1})")
        println(f"(${sequences._2})")
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
    def isSubstitutionMatrixCorrectSize(sequences: Array[String], 
                                        substitutionMatrix: Array[Array[Int]]): Boolean = {
        var answer: Boolean = true
        val rows: Integer = substitutionMatrix.length
        val columns: Integer = substitutionMatrix(0).length
        if (rows != columns) {
            logger.logWarn(f"Number of rows and columns in score matrix should be equal.")
            answer = false
        }

        val sequencesPar = sequences.par
        val res = sequencesPar.map(str => str.groupBy(c => c.toLower).map(e => (e._1, e._2.length)).toList.length)
        val requiredSize = res.toArray.max
        
        if (rows != requiredSize) {
            logger.logWarn(f"Invalid size of score matrix. Actual: ${rows}x${columns}, expected: ${requiredSize}x${requiredSize}")
            answer = false
        }

        answer
    }


    /*  Encode seqeuence by converting string to an array of integers
    */
    def encodeSequence(sequence: String): Array[Integer] = {
        val encoded = new mutable.ArrayBuilder.ofRef[Integer]

        for (base <- sequence) {
            base match {
                case 'A' => encoded += 0
                case 'C' => encoded += 1
                case 'G' => encoded += 2
                case 'T' => encoded += 3
            }
        }        

        encoded.result()
    }


    /*  Count sites without gaps
    */
    def getNumberOfResidues(sequence: String): Int = {
        sequence.count(c => c != '-')
    }


    /*  Get all alignments found using Smith-Waterman algorithm
    */
    def getSmithWatermanAlignments(sequences: Array[String],
                                endingPoints: Array[(Int, Int)],
                                moves: Array[String]): Array[(String, String)] = {
        var alignments: ArrayBuffer[(String, String)] = new ArrayBuffer[(String, String)]

        val firstSequence: String = sequences(0)
        val secondSequence: String = sequences(1)

        val numOfColumns: Int = secondSequence.length() + 1

        val firstAlignment: StringBuilder = new StringBuilder(Constants.EmptyString)
        val secondAlignment: StringBuilder = new StringBuilder(Constants.EmptyString)

        val leftShift: Int = 1
        val upShift: Int = numOfColumns
        val diagonalShift: Int = numOfColumns + 1

        for (point <- endingPoints) {
            var row: Int = point._1 - 1
            var column: Int = point._2 - 1

            var nextMoveId: Int = column + numOfColumns * row
            var keepReading: Boolean = false
            var nextMove: String = null

            if (row > 0 && column > 0) {
                keepReading = true
                nextMove = moves(nextMoveId)
            }

            while(keepReading && nextMove != null) {
                nextMove(0) match {
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

                if (row <= 0 || column <= 0) {
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
            alignments.+= ((firstAlignment, secondAlignment))
            firstAlignment.clear()
            secondAlignment.clear()
        }

        alignments.toArray
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

        val firstSequence = this.encodeSequence(sequences(0))
        val secondSequence = this.encodeSequence(sequences(1))

        val M: Int = firstSequence.length
        val N: Int = secondSequence.length

        val moves: Array[String] = Array.ofDim[String](rows * columns)
        val temp  = ArrayBuffer.fill((M + Constants.ArrayPadding) * (N + Constants.ArrayPadding))(0)
        val helper: Array[Array[Int]] = Array.ofDim[Int](M + Constants.ArrayPadding, N + Constants.ArrayPadding)

        for (m <- 0 to M) helper(m)(0) = 0
        for (n <- 0 to N) helper(0)(n) = 0

        var id = N + 2
        val start: Long = System.nanoTime()
        for (m <- 1 to M) {
            for  (n <- 1 to N) {
                val alignmentsMap: Map[Int, Int] = Map[Int, Int]()
                val prev: Int = helper(m-1)(n-1)

                val alignValue: Int = prev + substitutionMatrix(firstSequence(m-1))(secondSequence(n-1))
                if (alignValue >= 0) {
                    alignmentsMap += (Constants.Align -> alignValue)
                }

                val upper: Int = helper(m-1)(n)
                val verticalGap: Int = upper + penalty
                if (verticalGap >= 0) {
                    alignmentsMap += (Constants.VerticalGap -> verticalGap)
                }

                val left: Int = helper(m)(n-1)
                val horizontalGap: Int = left + penalty
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
        for (i <- helper.indices) {
            val alter = helper(i).zipWithIndex.filter { case (value, _) => value == maxScore }.map(_._2)
            arrayOfPairsBuffer ++= alter.map(value => (i, value))
        }

        alignments = this.getSmithWatermanAlignments(sequences, arrayOfPairsBuffer.toArray, moves)
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) {
            logger.logInfo(f"Alignments (${alignments.size}) using Smith-Waterman algorithm collected in ${duration} ms")
        }
        alignments
    }


    /*  Get all alignments found using Needleman-Wunsch algorithm
    */
    private def getNeedlemanWunschAlignments(sequences: Array[String],
                                            moves: Array[String]): Array[(String, String)] = {
        val firstSequence: String = sequences(0)
        val secondSequence: String = sequences(1)

        var row: Int = firstSequence.length
        var column: Int = secondSequence.length

        val leftShift: Int = 1
        val upShift: Int = secondSequence.length()
        val diagonalShift: Int = upShift + 1

        val firstAlignment: StringBuilder = new StringBuilder("")
        val secondAlignment: StringBuilder = new StringBuilder("")
        var alignments: ArrayBuffer[(String, String)] = new ArrayBuffer[(String, String)]()

        var nextMove: String = moves.last
        var nextMoveId: Int = moves.length - 1

        var run: Int = 0
        var step: Int = 0

        var keepReading: Boolean = true
        val toVisit: ArrayBuffer[(Int, Int, Char, Int)] = new ArrayBuffer()

        while (keepReading) {
            nextMove(0) match {
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

            if (nextMoveId >= 0 && row > 0 && column > 0) { // Sequences from the current path are not ready yet
                step += 1
                nextMove = moves(nextMoveId)

                if (nextMove.nonEmpty) {
                    for (possibleMove <- nextMove.substring(1)) toVisit += ((step, run, possibleMove, nextMoveId))
                }
            } else { // Sequences from the current path are ready
                if (row > 0) {
                    firstAlignment.insert(0, firstSequence.take(row))
                    secondAlignment.insert(0, "-" * row)
                }

                if (column > 0) {
                    secondAlignment.insert(0, secondSequence.take(column))
                    firstAlignment.insert(0, "-" * column)
                }

                alignments.+= ((firstAlignment, secondAlignment))
                firstAlignment.clear()
                secondAlignment.clear()

                if (toVisit.size == 0)  {
                    keepReading = false
                } else {
                    val newBranch = toVisit.remove(0)

                    firstAlignment.append(alignments(newBranch._2)._1.takeRight(newBranch._1))
                    secondAlignment.append(alignments(newBranch._2)._2.takeRight(newBranch._1))
                    nextMove = newBranch._3.toString()
                    nextMoveId = newBranch._4

                    run += 1
                    step = newBranch._1

                    val firstSteps = this.getNumberOfResidues(firstAlignment)
                    val secondSteps = this.getNumberOfResidues(secondAlignment)

                    row = firstSequence.length - firstSteps
                    column = secondSequence.length - secondSteps
                }
            }
        }

        alignments.toArray
    }


    /*  Find global alignment using Needleman-Wunsch algorithm
    */
    def needlemanWunschAlignment(sequences: Array[String],
                            substitutionMatrix: Array[Array[Int]],
                            gapPenalty: Integer = Constants.DefaultGapPenalty,
                            verbose: Boolean = logger.isVerbose()): Array[(String, String)] = {
        var alignments = Constants.EmptyAlignmentsArray
        val numberOfSequences = sequences.length
        if (numberOfSequences != 2) {
            logger.logWarn(f"Incorrect number of sequences. Actual: ${numberOfSequences}, expected: 2")
            return alignments
        }

        val firstSequence = this.encodeSequence(sequences(0))
        val secondSequence = this.encodeSequence(sequences(1))

        val M: Int = firstSequence.length
        val N: Int = secondSequence.length

        val moves: ArrayBuffer[String] = new ArrayBuffer[String]()
        val temp  = ArrayBuffer.fill((M + Constants.ArrayPadding) * (N + Constants.ArrayPadding))(0)
        val helper: Array[Array[Integer]] = Array.ofDim[Integer](M + Constants.ArrayPadding, N + Constants.ArrayPadding)

        for (m <- 0 to M) helper(m)(0) = m * gapPenalty
        for (n <- 0 to N) helper(0)(n) = n * gapPenalty

        var id = N + 2
        val start: Long = System.nanoTime()

        for (m <- 1 to M) {
            for  (n <- 1 to N) {
                val alignmentsMap: Map[Int, Int] = Map[Int, Int]()
                val prev: Int = helper(m-1)(n-1)

                val alignValue: Int = prev + substitutionMatrix(firstSequence(m-1))(secondSequence(n-1))
                alignmentsMap += (Constants.Align -> alignValue)

                val upper: Int =  helper(m-1)(n)
                val verticalGap: Int = upper + gapPenalty
                alignmentsMap += (Constants.VerticalGap -> verticalGap)

                val left: Int = helper(m)(n-1)
                val horizontalGap: Int = left + gapPenalty
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

        alignments = this.getNeedlemanWunschAlignments(sequences, moves)
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) {
            logger.logInfo(f"Alignments: (${alignments.size}) using Needleman-Wunsch algorithm collected in ${duration} ms")
        }
        alignments
    }
}
