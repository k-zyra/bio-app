package utils


object ScoreCalculator {
    val logger = new Logger("ScoreCalculator")
    
    val Phred33Offset = 33
    val Phred64Offset = 64

    
    /** Get offset of  
     *  Detect Phred offset
     */
    def getOffset(sample: String): Double = {
        if (sample.charAt(0) > 63) return Phred33Offset
        else return Phred64Offset
    }


    /** Calculate Phred33 quality score
     *  Used in Illumina, Ion Torrent, PacBio and Sanger
     */
    def getPhred33Quality(P: Double): Int = {
        return ((-10) * math.log10(P)).toInt
    }


    /**  Calculate Phred64 quality score
     *   Used in old Illumina
     */
    def getPhred64Quality(P: Double): Int = {
        return ((-10) * math.log10(P)).toInt
    }

     
    /** Calculate Phred33 score and convert to ASCII
     *  Used in Illumina, Ion Torrent, PacBio and Sanger
     */
    def getPhred33Ascii(P: Double): Char = {
        return ((-10) * math.log10(P) + Phred33Offset).toChar
    }


    /**  Calculate Phred64 score and convert to ASCII
     *   Used in old Illumina
     */
    def getPhred64Ascii(P: Double): Char = {
        return ((-10) * math.log10(P) + Phred64Offset).toChar
    }

    
    /**  Get base calling error
     *  Used in Illumina, Ion Torrent, PacBio and Sanger
     */
    def getBaseCalling33Error(Q: Char): Double = {
        val base = 10
        val offset = 33
        val Qint = Q.toInt - offset
        
        val exp = (-Qint/10)
        return math.pow(base, exp)
    }   

    
    /**  Gets base calling error
     *   Used in old Illumina
     */
    def getBaseCalling64Error(Q: Char): Double = {
        val base = 10
        val offset = 64
        val Qint = Q.toInt - offset
                
        val exp = (-Qint/10)
        return math.pow(base, exp)
    }
}
