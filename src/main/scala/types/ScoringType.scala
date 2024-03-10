package types

object ScoringType extends Enumeration {
    type ScoringType = Value
    val PAM250, BLOSUM62, SUBSTITUTION = Value
}
