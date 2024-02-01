package utils

import bio.datatypes.File
import bio.datatypes.Sequence

import bio.ukkonen._
import bio.ukkonen.UkkonenNode


object Constants {
    val BASES: Array[Char] = Array('A', 'C', 'G', 'T')
    val AMBIGUOUS: Char = 'N'
    
    // General
    val SUCCESS: Boolean = true
    val FAILURE: Boolean = true
    val FORCE: Boolean = true

    val NOT_FOUND_I: Integer = -1
    val NOT_FOUND_F: Float = -1
    val NOT_EXIST: Integer = 0

    val PARAMETER_UNSPECIFIED: Integer = -1

    val ARRAY_PADDING: Integer = 1
    val STRING_OFFSET: Integer  = 1

    val MIN_THRESHOLD: Integer = 1
    val WEAK_KMER_THRESHOLD: Integer = 10
    val SOLID_KMER_THRESHOLD: Integer = 90

    val NANO_IN_MILLIS: Integer = 1000

    // Alignment constants
    val DEFAULT_GAP_PENALTY: Integer = -1
    val DEFAULT_MISMATCH_PENALTY: Integer = -1
    val DEFAULT_MATCH_REWARD: Integer = 1

    val ALIGN: Integer = 1
    val HORIZONTAL_GAP: Integer = 2
    val VERTiCAL_GAP: Integer = 3

    // Cluster constants
    val NUMBER_OF_CLUSTERS: Integer = 4
    val NUMBER_OF_ROWS: Integer = 20

    val PREFIX_BASED: Integer = 0
    val CONTENT_BASED: Integer = 1
    val FREQUENCY_BASED: Integer = 2

    // Numerical constants
    val EMPTY_INTEGER_ARRAY = Array[Integer]()
    val EMPTY_FLOAT_ARRAY = Array[Float]()

    // String constants 
    val EMPTY_STRING: String = ""
    val EMPTY_SENTINEL: String = ""
    val EMPTY_STRING_ARRAY = Array[String]()
    
    val DEFAULT_SENTINEL: String = "$"
    
    val HEADER_TAG: String = "@"
    val SCORE_TAG: String = ">"
    val SEQID_TAG: String = "+"

    // File constants
    val EMPTY_FILE: File = new File(".", "UNKNOWN", Array[Sequence]())

    val FASTA_EXT: String = ".fasta"
    val FASTQ_EXT: String = ".fastq"
    val SUPPORTED_TYPES: Array[String] = Array(this.FASTA_EXT, this.FASTQ_EXT)

    // Tree constants
    val NODE_INDEX: Integer = -1
    val EDGE_START: Integer = 0
    val EDGE_END: Integer = -1

    val INITIAL_STEP: Integer = 0    
    val NOT_FOUND_INDEX: Integer = -1

    val ZERO_LENGTH: Integer = 0
    val ZERO_REMAINING: Integer = 0
    
    val ROOT: UkkonenNode = new UkkonenNode(0)
    val DUMMY_NODE: UkkonenNode = new UkkonenNode(NODE_INDEX)
    val DUMMY_EDGE: Integer = -1

    // Utils 
    val DEFAULT_PROMPT: String = "bio-app> "

    val DATA_DIR = "C:\\Users\\karzyr\\Desktop\\bio-app\\data"
    val EXAMPLE_SUBSTITUTION_MATRIX: String = "substitutionMatrix.xml"
    val EMPTY_SUBSTITUTION_MATRIX: String = "substitutionMatrix_template.xml"
}
