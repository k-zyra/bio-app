package types

object DistanceType extends Enumeration {
    type DistanceType = Value
    val HAMMING, JACCARD, JARO, LEVENSHTEIN = Value
}