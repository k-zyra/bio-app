package bio.align.multiple

/* External imports */
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/* Internal imports */
import misc.Logger
import types.Biotype.{Alignment, CurrentAlignment}



object BlockMutation {
    private val logger = new Logger("MSA_BlockMutation")


    /* Perform block shuffle for an alignment
    *  Gaps will be moved till they will be merged with different gaps
    */
    def blockShift(specimen: Alignment, shifts: Int = 1): Unit = {

    }


    /* Modify single specimen by local realignment in a specific block
    */
    def localRealignment(specimen: Alignment): Alignment = {
        val sequenceLength: Int = specimen(0).length
        println(s"Sequence length: ${sequenceLength}")
        println(s"Max window size: ${(sequenceLength/4)+3}")
        val windowSize: Int = Random.nextInt(sequenceLength / 4) + 3
        val position: Int = Random.nextInt(sequenceLength - windowSize)
        val sequenceId: Int = Random.nextInt(specimen.length)
        val sequence: String = specimen(sequenceId)
        if (!sequence.contains('-')) return specimen

        val gapsIds: Array[Int] = GapMutation.getGapIndices(sequence).toArray

        println(s"Chosen sequence ${sequenceId}  : ${sequence}")
        println(s"Window size: ${windowSize}")
        println(s"Starting from: ${position}")
        println(s"Get window: ${sequence.substring(position, position+windowSize)}")
        println(s"Gap indices: ")
        gapsIds.foreach(println)

        for (i <- 0 to windowSize) {
            println(s"Considering ${i} relocation")
//            var newWindow: String = randomSequence.substring()
        }

        val child: ArrayBuffer[String] = new ArrayBuffer[String]()
        return child.result().toArray
    }


    /* Modify single specimen by gap insertion
    */
    def insertGap(specimen: Alignment): Alignment = {
        //        val gapLength: Int = Random.nextInt(specimen(0).length) + 1
        //        val gapLength: Int = Random.nextInt(Config.initialAverageLength/2) + 1
        val gapLength: Int = Random.nextInt(specimen(0).length / 2) + 1
        val gapPosition: Int = Random.nextInt(specimen(0).length)

        val mutant: ArrayBuffer[String] = new ArrayBuffer[String]()
        for (sequence <- specimen) {
            mutant += new String(sequence.take(gapPosition) + ("-" * gapLength) + sequence.substring(gapPosition))
        }

        mutant.result().toArray
    }


    /* Remove block of gaps from one sequence
    */
    def removeGapBlock(specimen: Alignment): Alignment = {
        val sequenceId: Int = Random.nextInt(specimen.length)
        val mutatedSeqeunce: String = specimen(sequenceId)
        if (!mutatedSeqeunce.contains('-')) return specimen

        val gapsIds = mutatedSeqeunce.dropRight(1).zipWithIndex.filter { case (c, _) => c == '-' }.map(_._2)
        if (gapsIds.isEmpty) return specimen
        val gapId: Int = Random.shuffle(gapsIds).take(1)(0)

        val originalLength: Int = mutatedSeqeunce.length
        val mutant: ArrayBuffer[String] = specimen.clone().to[ArrayBuffer]
        mutant(sequenceId) = (mutatedSeqeunce.take(gapId) + mutatedSeqeunce.substring(gapId).dropWhile(_ == '-')).padTo(originalLength, '-')

        mutant.toArray
    }


    /* Remove large blocks of gaps from generated sequences
    */
    def trimRedundantGaps(specimen: Alignment): Alignment = {
        var residuesIds = specimen(0).zipWithIndex.filter { case (c, _) => c != '-' }.map(_._2)

        for (id <- 1 until specimen.size) {
            residuesIds = residuesIds union specimen(id).zipWithIndex.filter { case (c, _) => c != '-' }.map(_._2)
            if (residuesIds.isEmpty) return specimen
        }

        val mutant: CurrentAlignment = new CurrentAlignment
        for (id <- specimen.indices) {
            val trimmed = new StringBuilder("")

            for (i <- residuesIds.sorted.distinct) trimmed += specimen(id)(i)
            mutant += trimmed.toString()
            trimmed.clear()
        }

        mutant.toArray
    }
}
