package utils


object ScoreCalculator {
    val logger = new Logger("ScoreCalculator")
    
    val phred33offset = 33
    val phred64offset = 64

    
    /** Get offset of  
     *  Detect Phred offset
     */
    def getOffset(sample: String) : Double = {
        if (sample.charAt(0) > 63) return phred33offset
        else return phred64offset
    }


    /** Calculate Phred33 score
     */
    def getPhred33Score(P: Double) : Char = {
        return ((-10) * math.log10(P) + phred33offset).toChar
    }


    /**  Calculate Phred64 score
     */
    def getPhred64Score(P: Double) : Char = {
        return ((-10) * math.log10(P) + phred64offset).toChar
    }

    
    /**  Get base calling error
     */
    def getBaseCalling33Error(Q: Char) : Double = {
        val base = 10
        val offset = 33
        val Qint = Q.toInt - offset
        
        val exp = (-Qint/10)
        return math.pow(base, exp)
    }   

    /**  Gets base calling error
     */
    def getBaseCalling64Error(Q: Char) : Double = {
        val base = 10
        val offset = 64
        val Qint = Q.toInt - offset
                
        val exp = (-Qint/10)
        return math.pow(base, exp)
    }
}
