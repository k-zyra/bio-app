package bio.align.multiple

import misc.Logger
import types.Biotype.Alignment

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object BlockMutation {
    private val logger = new Logger("MSA_BlockMutation")


    /* Perform block shuffle for an alignment
    *  Gaps will be moved till they will be merged with different gaps
    */
    def blockShuffle(alignment: Alignment,
                     shifts: Int = 1): Unit = {

    }


    /* Modify single specimen by gap insertion
    */
    def localRealignment(specimen: Alignment): Alignment = {
        val sequenceLength: Int = specimen(0).length
        val windowSize: Int = Random.nextInt(sequenceLength / 5)
        val position: Int = Random.nextInt(sequenceLength - windowSize)

        println(s"Window size: ${windowSize}")
        println(s"Starting from: ${position}")

        //        for (sequence <- specimen) {
        //
        //        }

        val child: ArrayBuffer[String] = new ArrayBuffer[String]()
        return child.result().toArray
    }
}
