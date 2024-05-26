package misc

/* Internal imports */
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

    val DISABLED: Boolean = false
    val ENABLED: Boolean = true

    val NotFoundInteger: Integer = -1
    val NotFoundFloat: Float = -1

    val ParameterUnspecified: Integer = -1

    val ArrayPadding: Integer = 1
    val StringOffset: Integer = 1

    val MinThreshold: Integer = 1
    val WeakKmerThreshold: Integer = 10

    val PhredMaxThreshold: Integer = 65
    val PhreadDefaultThreshold: Integer = 30

    val NanoInMillis: Float = 1000000.0f

    // Alignment constants
    val DefaultGapPenalty: Integer = -5
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
    val DataDir = "C:\\Users\\karzyr\\Desktop\\bio-app\\data"
    val LocaLDefaultMatrix: String = "substitutionMatrix_local.xml"
    val GlobalDefaultMatrix: String = "substitutionMatrix_global.xml"
    val EmptySubstitutionMatrix: String = "substitutionMatrix_template.xml"
}
