package scoring

import types.SequenceType

object SubstitutionMatrix extends Scoring {
    def loadMatrix(filename: String): ScoringMatrix = {
        return Map[String, Int]()
    }

    def getDefaultMatrix(sequenceType: SequenceType.SequenceType): ScoringMatrix = {
        sequenceType match {
            case SequenceType.DNA => return DnaDefaultMatrix
        }
    }

    val DnaDefaultMatrix: ScoringMatrix = Map(
        "AA" -> 10, "AG" -> -1, "AC" -> -3, "AT" -> -4,
        "GA" -> -1, "GG" -> 7, "GC" -> -5, "GT" -> -3,
        "CA" -> -3, "CG" -> -5, "CC" -> 9, "CT" -> 0,
        "TA" -> -4, "TG" -> -5, "TC" -> 0, "TT" -> 8
    )

    val RnaDefaultMatrix: ScoringMatrix = Map()
    val ProteinDefaultMatrix: ScoringMatrix = Map()
}