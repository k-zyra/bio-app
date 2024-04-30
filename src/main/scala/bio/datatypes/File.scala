package bio.datatypes



class File(_path: String, _filetype: String, _sequences: Array[Sequence]) {
    private val path: String = _path
    private val filetype: String = _filetype
    private val sequences: Array[Sequence] = _sequences


    /** Print File class header
     */
    override def toString(): String = {
        s"[$filetype file] $path"
    }


    /** Get 'path' class member
     */
    def getPath(): String = {
        this.path
    }


    /** Get 'filetype' class member 
     */
    def getFileType(): String = {
        this.filetype
    }


    /** Get all sequences from given file 
     */
    def getSequences(): Array[Sequence] = {
        this.sequences
    }


    /** Get only headers from list of sequences
     */
    def getHeaders(): Array[String] = {
        this.sequences.map(_.header)
    }


    /** Get only reads from list of sequences
     */
    def getReads(): Array[String] = {
        sequences.map(_.read)
    }


    /** Get only scores from list of sequences
     */
    def getScores(): Array[String] = {
        sequences.map(_.score)
    }

    
    /** Return all sequences with quality scores
     */
    def getReadsAndScores(): Array[(String, String)] = {
        this.getReads() zip this.getScores()
    }


    /** Get number of reads in a file
     */
    def getNumberOfReads(): Integer = {
        this.getReads().length
    }


    /** Get number of scores in a file
     */
    def getNumberOfScores(): Integer = {
        this.getScores().length
    }
}
