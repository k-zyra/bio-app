package misc

import bio.datatypes.{File, Sequence}
import bio.ukkonen.UkkonenNode

object Constants {
    val Ambiguous: Char = 'N'

    val Nucleobases: Array[Char] = Array('A', 'C', 'G', 'T')
    val NucleobasesExtended: Array[Char] = Array('-', 'A', 'C', 'G', 'T')

    val AminoAcids: Array[Char] = Array('A', 'C', 'D', 'E',
                                    'F', 'G', 'H', 'I', 'K', 'L',
                                    'M', 'N', 'O', 'P', 'Q', 'R',
                                    'S', 'T', 'U', 'V', 'W', 'Y')
    val AminoAcidsExtended: Array[Char] = Array('-', 'A', 'C', 'D', 'E',
                                        'F', 'G', 'H', 'I', 'K', 'L',
                                        'M', 'N', 'O', 'P', 'Q', 'R',
                                        'S', 'T', 'U', 'V', 'W', 'Y')

    // General
    val Success: Boolean = true
    val Failure: Boolean = true
    val Force: Boolean = true

    val NotFoundInteger: Integer = -1
    val NotFoundFloat: Float = -1
    val NotExist: Integer = 0

    val ParameterUnspecified: Integer = -1

    val ArrayPadding: Integer = 1
    val StringOffset: Integer = 1

    val MinThreshold: Integer = 1
    val WeakKmerThreshold: Integer = 10
    val SolidKmerThreshold: Integer = 90

    val PhredMaxThreshold: Integer = 65
    val PhreadDefaultThreshold: Integer = 30

    val NanoInMillis: Integer = 1000000

    // Alignment constants
    val DefaultGapPenalty: Integer = -1
    val DefaultGapExtensionPenalty: Integer = 0
    val DefaultMismatchPenalty: Integer = -1
    val DefaultMatchReward: Integer = 1

    val Align: Int = 1
    val HorizontalGap: Int = 2
    val VerticalGap: Int = 3

    // Cluster constants
    val NumberOfClusters: Integer = 4
    val NumberOfRows: Integer = 20

    val PrefixBased: Integer = 0
    val ContentBased: Integer = 1
    val FrequencyBased: Integer = 2

    // Numerical constants
    val EmptyIntegerArray = Array[Integer]()
    val EmptyFloatArray = Array[Float]()

    // String constants
    val EmptyString: String = ""
    val DefaultSentinel: String = "$"

    val EmptyStringArray = Array[String]()
    val EmptyAlignmentsArray = Array[(String, String)]()

    val HeaderTag: String = "@"
    val ScoreTag: String = ">"
    val SequenceIdTag: String = "+"
    val TfaHeaderTag: String = ">"

    // File constants
    val EmptyFile: File = new File(".", "UNKNOWN", Array[Sequence]())

    val FastaExtension: String = ".fasta"
    val FastqExtension: String = ".fastq"
    val TfaExtension: String = ".tfa"
    val SupportedTypes: Array[String] = Array(this.FastaExtension, this.FastqExtension, this.TfaExtension)

    // Tree constants
    val NodeIndex: Integer = -1
    val EdgeStart: Integer = 0
    val EdgeEnd: Integer = -1

    val InitialStep: Integer = 0
    val IndexNotFound: Integer = -1

    val ZeroLength: Integer = 0
    val ZeroRemaining: Integer = 0

    val Root: UkkonenNode = new UkkonenNode(0)
    val DummyNode: UkkonenNode = new UkkonenNode(NodeIndex)
    val DummyEdge: Integer = -1

    // Utils
    val DefaultPrompt: String = "bio-app> "

    val DataDir = "C:\\Users\\karzyr\\Desktop\\bio-app\\data"
    val ExampleSubstitutionMatrix: String = "substitutionMatrix.xml"
    val EmptySubstitutionMatrix: String = "substitutionMatrix_template.xml"
}
