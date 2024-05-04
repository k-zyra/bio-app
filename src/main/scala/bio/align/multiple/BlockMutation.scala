package bio.align.multiple

/* External imports */
import scala.collection.mutable.ArrayBuffer
import scala.language.implicitConversions
import scala.util.Random

/* Internal imports */
import misc.Logger
import types.Biotype.{Alignment, CurrentAlignment}



object BlockMutation {
    private val logger = new Logger("MSA_BlockMutation")


    implicit def convertToString(stringBuilder: StringBuilder): String = {
        stringBuilder.toString()
    }


    /* Find the positions in sequences on which the identical substring occur
    */
    private def findCorrespondingSites(alignment: Alignment, substr: String): Array[Int] = {
        val indicesArray = new ArrayBuffer[Int]()

        for (i <- alignment.indices) {
            val sliceId: Int = alignment(i).indexOfSlice(substr)
            if (sliceId >= 0) indicesArray += sliceId
            else {
                indicesArray.clear()
                return indicesArray.toArray
            }
        }

        indicesArray.toArray
    }


    private def selectBlock(specimen: Alignment): (Int, Int) = {
        val sequenceLength: Int = specimen(0).length
        val position: Int = Random.nextInt(sequenceLength)
        val maxWindowLength: Int = (sequenceLength - position).min(sequenceLength / 4)
        val windowSize: Int = maxWindowLength

        (position, windowSize)
    }


    def realignResidues(specimen: Alignment): Alignment = {
        val sequenceLength: Int = specimen(0).length
        var position: Int = Random.nextInt(sequenceLength)
        var refChar: Char = specimen(0)(position)

        while (refChar == '-') {
            position = Random.nextInt(sequenceLength)
            refChar = specimen(0)(position)
        }

        val mutant: CurrentAlignment = new CurrentAlignment
        for (i <- specimen.indices) {
            mutant += specimen(i).take(position)
        }
        mutant(0) += refChar

        var shifts: Map[Char, Int] = Map.empty[Char, Int]
        var added: Int = 1
        for (i <- 1 until specimen.length) {
            val consideredChar: Char = specimen(i)(position)
            val toBeAdded: StringBuilder = new StringBuilder()

            if (consideredChar == refChar || consideredChar == '-') {
                mutant(i) += consideredChar
            } else {
                if (shifts.contains(consideredChar)) {
                    toBeAdded ++= "-" * shifts.getOrElse(consideredChar, 0)
                    toBeAdded += consideredChar
                    mutant(i) ++= toBeAdded
                } else {
                    mutant(0) ++= "-"

                    toBeAdded ++= "-" * added
                    toBeAdded += consideredChar
                    mutant(i) ++= toBeAdded

                    shifts += (consideredChar -> added)
                    added += 1
                }
            }
        }

        val refLength: Int = mutant(0).length
        for (i <- specimen.indices) {
            val diff = refLength - mutant(i).length
            mutant(i) += "-" * diff
            mutant(i) += specimen(i).substring(position + 1)
        }

        mutant.toArray
    }

    /* Modify single specimen by gap insertion
    */
    def insertGap(specimen: Alignment): Alignment = {
        val gapLength: Int = Random.nextInt(Config.initialAverageLength/2) + 1
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


    /* Adjust chosen residues in multiple sequences to keep them in one line
    */
    def adjustResidues(specimen: Alignment): Alignment = {
        val specimenLength: Int = specimen(0).length
        val maxPosition: Int = specimenLength
        var residueId: Int = Random.nextInt(maxPosition)

        val maxLength: Int = (specimenLength - residueId).min(specimenLength / 4)
        val numberOfResidues: Int = Random.nextInt(maxLength) + 1

        while (specimen(0)(residueId) == '-') {
            residueId = Random.nextInt(maxPosition)
        }

        var substr: StringBuilder = new StringBuilder()
        substr ++= specimen(0).slice(residueId, residueId + numberOfResidues)

        var sites = this.findCorrespondingSites(specimen, substr)
        while(sites.isEmpty && substr.length > 1) {
            substr = substr.deleteCharAt(substr.length - 1)
            sites = this.findCorrespondingSites(specimen, substr)
        }

        if (sites.isEmpty) return specimen

        val alignTo: Int = sites.max
        val mutant: CurrentAlignment = new CurrentAlignment
        for (i <- sites.indices) {
            val site: Int = sites(i)
            val gapsToInsert: Int = alignTo - site

            mutant += specimen(i).take(site) + "-" * gapsToInsert + specimen(i).substring(site)
        }

        Utils.adjustAlignment(mutant.toArray)
    }
}
