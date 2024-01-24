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


    /** Get only reads from List of Sequences
     */
    def getReads(): Array[String] = {
        return sequences.map(_.read).toArray    
    }


    /** Get number of reads in a file
     */
    def getNumberOfReads(): Integer = {
        return this.getReads().length
    }


    /** Get only headers from List of Sequences
     */
    def getHeaders(): Array[String] = {
        return this.sequences.map(_.header).toArray
    }
}
