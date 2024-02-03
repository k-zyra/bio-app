package bio.datatypes


class File(_path: String, _filetype: String, _sequences: Array[Sequence]) {
    private val path: String = _path
    private val filetype: String = _filetype
    private val sequences: Array[Sequence] = _sequences


    /** Print File class header
     */
    override def toString(): String = {
        return s"[$filetype file] $path"
    }


    /** Get 'path' class member
     */
    def getPath(): String = {
        return this.path
    }


    /** Get 'filetype' class member 
     */
    def getFileType(): String = {
        return this.filetype
    }


    /** Get all sequences from given file 
     */
    def getSequences(): Array[Sequence] = {
        return this.sequences
    }


    /** Get only headers from list of sequences
     */
    def getHeaders(): Array[String] = {
        return this.sequences.map(_.header).toArray
    }


    /** Get only reads from list of sequences
     */
    def getReads(): Array[String] = {
        return sequences.map(_.read).toArray    
    }


    /** Get only scores from list of sequences
     */
    def getScores(): Array[String] = {
        return sequences.map(_.score).toArray    
    }

    
    /** Return all sequences with quality scores
     */
    def getReadsAndScores(): Array[(String, String)] = {
        return this.getReads() zip this.getScores()
    }


    /** Get number of reads in a file
     */
    def getNumberOfReads(): Integer = {
        return this.getReads().length
    }


    /** Get number of scores in a file
     */
    def getNumberOfScores(): Integer = {
        return this.getScores().length
    }
}
