package bio.align.multiple

/* External imports */
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/* Internal imports */
import misc.{Constants, Logger}
import types.Biotype.{Alignment, CurrentAlignment}



object GapMutation {
    private val logger = new Logger("MSA_GapMutation")


    /* Modify single specimen by gap insertion
    */
    def insertGap(specimen: Alignment): Alignment = {
        val gapLength: Int = Random.nextInt(specimen(0).length) + 1
        val gapPosition: Int = Random.nextInt(specimen(0).length)

        val mutant: ArrayBuffer[String] = new ArrayBuffer[String]()
        for (sequence <- specimen) {
            mutant += new String(sequence.take(gapPosition) + ("-" * gapLength) + sequence.substring(gapPosition))
        }

        return mutant.result().toArray
    }


    /* Modify single specimen by gap of length 1 insertion
    */
    def insertSingleGap(specimen: Alignment): Alignment = {
        val gapPosition: Int = Random.nextInt(specimen(0).length)
        val sequenceId: Int = Random.nextInt(specimen.length)

        val mutant: Array[String] = specimen.clone()
        val changed: String = specimen(sequenceId).take(gapPosition) + "-" + specimen(sequenceId).substring(gapPosition)
        mutant(sequenceId) = changed

        return Utils.adjustAlignment(mutant).toArray
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

        return mutant.toArray
    }


    /* Remove gaps from the sequences at the certain position
    */
    def removeGap(specimen: Alignment): Alignment = {
        var gapsIds = specimen(0).zipWithIndex.filter { case (c, _) => c == '-' }.map(_._2)

        for (id <- 1 until specimen.size) {
            gapsIds = gapsIds intersect specimen(id).zipWithIndex.filter { case (c, _) => c == '-' }.map(_._2)
            if (gapsIds.isEmpty) return specimen
        }

        val gapIndex: Int = Random.shuffle(gapsIds).take(1)(0)
        val mutant: CurrentAlignment = new CurrentAlignment
        for (id <- specimen.indices) {
            mutant += specimen(id).take(gapIndex) + specimen(id).substring(gapIndex + 1)
        }

        return mutant.toArray
    }


    /* Remove one gap from the middle of randomly chosen sequence
    *  Check if other sequences in this alignment could be trimmed to keep the same order
    */
    def removeSingleGap(specimen: Alignment): Alignment = {
        val sequenceId: Int = Random.nextInt(specimen.length)

        val gapsIds = specimen(sequenceId).zipWithIndex.filter { case (c, _) => c == '-' }.map(_._2)
        if (gapsIds.isEmpty) return specimen

        val gapIndex: Int = Random.shuffle(gapsIds).take(1)(0)
        val mutant: ArrayBuffer[String] = specimen.clone().to[ArrayBuffer]
        mutant(sequenceId) = specimen(sequenceId).take(gapIndex) + specimen(sequenceId).substring(gapIndex + 1) + "-"

        return mutant.toArray
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

        return mutant.toArray
    }


    /* Move gap in one of the sequences from certain position
    */
    def moveSingleGap(specimen: Alignment): Alignment = {
        val sequenceId: Int = Random.nextInt(specimen.length)
        val numberOfGaps: Int = specimen(sequenceId).count(_ == '-')
        if (numberOfGaps == 0) return specimen

        val idBefore: Int = Random.nextInt(numberOfGaps)
        val gapIndex = specimen(sequenceId).zipWithIndex.filter { case (c, _) => c == '-' }.lift(idBefore).map(_._2).get

        val mutatedSequence: String = specimen(sequenceId).take(gapIndex) + specimen(sequenceId).substring(gapIndex + 1) + "-"
        val mutant = specimen.clone()
        mutant(sequenceId) = mutatedSequence

        return Utils.adjustAlignment(mutant).toArray
    }


    /*
    */
    def adjustGaps(alignment: Alignment,
                   windowSize: Int = 1): Unit = {

    }


    /* Extend existing gap
    */
    def extendGap(specimen: Alignment,
                  verbose: Boolean = logger.isVerbose()): Alignment = {
        val numberOfSequences: Int = specimen.length
        val sequenceId: Int = Random.nextInt(numberOfSequences)

        val start: Long = System.nanoTime()
        val lastLetterIndices: Int = specimen(sequenceId).lastIndexWhere(_ != '-')
        val subsequence: String = specimen(sequenceId).take(lastLetterIndices)

        val numberOfGaps: Int = subsequence.count(_ == '-')
        if (numberOfGaps == 0) return specimen

        val gapsIds = this.getGapIndices(subsequence)
        val gapId: Int = Random.shuffle(gapsIds).take(1)(0)

        val extension: Int = Random.nextInt(Config.initialAverageLength/2) + 1
        val mutatedSequence: String = specimen(sequenceId).take(gapId) + ("-" * extension) + specimen(sequenceId).substring(gapId)
        val mutant: Alignment = specimen.clone()
        mutant(sequenceId) = mutatedSequence
        val duration: Float = (System.nanoTime() - start) / Constants.NanoInMillis

//        if (verbose) logger.logInfo(s"Time spent in extendGaps: ${duration} ms")
        return Utils.adjustAlignment(mutant).toArray
    }


    /* Get indices of gaps in a sequence
    */
    def getGapIndices(sequence: String): IndexedSeq[Int] = {
        return sequence.zipWithIndex.filter { case (c, _) => c == '-' }.map(_._2)
    }
}
