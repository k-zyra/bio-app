package bio.align.multiple

import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import scala.util.control.Breaks.{break, breakable}

import misc.{Constants, Logger}
import types.Biotype.Alignment



object Crossover {
    private val logger = new Logger("MSA_Crossover")

    /*  Perform a one-point crossover by merging parents' alignments
    *   Return only the best child
    */
    def onePoint(firstParent: Alignment,
                          secondParent: Alignment,
                          verbose: Boolean = logger.isVerbose()): Alignment = {
        assert(firstParent.length == secondParent.length)

        val numberOfAlignments: Integer = firstParent.length
        val maxCutPosition: Integer = firstParent(0).count(_ != '-') - 1
        val cutPoint: Integer = Random.nextInt(maxCutPosition) + 1

        val firstChild: ArrayBuffer[String] = ArrayBuffer[String]()
        val secondChild: ArrayBuffer[String] = ArrayBuffer[String]()

        val start = System.nanoTime()
        for (i <- 0 until numberOfAlignments) {
            val firstSequence: String = firstParent(i)
            val secondSequence: String = secondParent(i)
            val ids = new ArrayBuffer[Int]()

            for (sequence <- Array(firstSequence, secondSequence)) {
                var tmpCutpoint = cutPoint

                breakable {
                    for (id <- 0 until sequence.length()) {
                        if (sequence(id) != '-') tmpCutpoint -= 1
                        if (tmpCutpoint == 0) {
                            ids += id
                            break
                        }
                    }
                }
            }

            val firstCutPoint: Int = ids(0)
            val secondCutPoint: Int = ids(1)

            val firstChildSequence = firstSequence.take(firstCutPoint) + secondSequence.substring(secondCutPoint)
            val secondChildSequence = secondSequence.take(secondCutPoint) + firstSequence.substring(firstCutPoint)

            firstChild += firstChildSequence
            secondChild += secondChildSequence
        }
        val duration: Float = (System.nanoTime() - start) / Constants.NanoInMillis

        val firstChildArray: Alignment = Utils.adjustAlignment(firstChild.toArray)
        val secondChildArray: Alignment = Utils.adjustAlignment(secondChild.toArray)

        if (verbose) logger.logInfo(f"One-point crossover duration: ${duration} ns")
        return Fitness.chooseChild(firstChildArray, secondChildArray)
    }


    def uniform(firstParent: Alignment,
                         secondParent: Alignment,
                         verbose: Boolean = logger.isVerbose()): Unit = {
        assert(firstParent.length == secondParent.length)


        //        return this.chooseChild()
    }


    /* Modify single specimen by gap insertion
    */
    def sequential(firstParent: Alignment,
                   secondParent: Alignment,
                   verbose: Boolean = logger.isVerbose()): Unit = {

    }
}
