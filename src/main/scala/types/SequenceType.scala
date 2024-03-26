package types

object SequenceType extends Enumeration {
    type SequenceType = Value
    val UNKNOWN, DNA, RNA, PROTEIN = Value
}
