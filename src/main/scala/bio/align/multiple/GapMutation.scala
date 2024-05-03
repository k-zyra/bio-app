package bio.align.multiple

/* External imports */
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/* Internal imports */
import misc.Logger
import types.Biotype.{Alignment, CurrentAlignment}



object GapMutation {
    private val logger = new Logger("MSA_GapMutation")


    /* Modify single specimen by gap of length 1 insertion
    */
    def insertSingleGap(specimen: Alignment): Alignment = {
        val gapPosition: Int = Random.nextInt(specimen(0).length)
        val sequenceId: Int = Random.nextInt(specimen.length)

        val mutant: Array[String] = specimen.clone()
        val changed: String = specimen(sequenceId).take(gapPosition) + "-" + specimen(sequenceId).substring(gapPosition)
        mutant(sequenceId) = changed

        Utils.adjustAlignment(mutant)
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

        mutant.toArray
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

        mutant.toArray
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

        Utils.adjustAlignment(mutant)
    }


    /* Extend existing gap
    */
    def extendGap(specimen: Alignment): Alignment = {
        val numberOfSequences: Int = specimen.length
        val sequenceId: Int = Random.nextInt(numberOfSequences)

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

        Utils.adjustAlignment(mutant)
    }


    /* Get indices of gaps in a sequence
    */
    def getGapIndices(sequence: String): IndexedSeq[Int] = {
        sequence.zipWithIndex.filter { case (c, _) => c == '-' }.map(_._2)
    }
}
