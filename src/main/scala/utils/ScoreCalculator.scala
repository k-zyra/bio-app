package utils

/* External imports */
import scala.collection.mutable.ArrayBuffer
import breeze.linalg.sum
import algebra.lattice.Bool


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


    /**  Convert Phred score from ASCII to Int
     */
    def convertPhredToDouble(Q: Char, base: Integer): Int = {
        return Q.toInt - base
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


    /**  Calculate mean quality 
     */
    def getMeanQuality(quality: String, base: Int): Double = {
        val numberOfElements: Int = quality.length()
        var sumOfQualities: Double = 0
        sumOfQualities = quality.map(element => element.toInt).reduceLeft(_+_)
        return (sumOfQualities/numberOfElements)
    }                  


    /** Filter kmers in sequential manner
     *  Get only sequences with mean Phred quality score not worse than given threshold
     *  Return an array containing filtered kmers
     */
    def filterByMeanQualitySequential(sequences: Array[(String, String)],
                                    threshold: Integer = Constants.PhreadDefaultThreshold,
                                    base: Integer,
                                    verbose: Boolean = false): Array[(String, String)] = {
        if (threshold < 0 || threshold > Constants.PhredMaxThreshold) {
            this.logger.logError(f"Incorrect value of threshold: ${threshold}, should be in range [0, ${base}]")
            return Array[(String, String)]()
        }

        var filtered = new ArrayBuffer[(String, String)]
        val start: Long = System.nanoTime()
        for (sequence <- sequences) {
            if (this.getMeanQuality(sequence._2, base) >= threshold) {
                filtered += sequence
            }
        }
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) println(f"Filtered by mean quality in $duration ms")
        return filtered.result().toArray                                  
    }


    /** Filter sequences in parallel manner
     *  Get only kmers with mean Phred quality score not worse than given threshold
     *  Return an array containing filtered kmers
     */
    def filterByMeanQualityParallel(sequences: Array[(String, String)],
                                    threshold: Integer = Constants.PhreadDefaultThreshold,
                                    base: Integer,
                                    verbose: Boolean = false): Array[(String, String)] = {
        if (threshold < 0 || threshold > Constants.PhredMaxThreshold) {
            this.logger.logError(f"Incorrect value of threshold: ${threshold}, should be in range [0, ${base}]")
            return Array[(String, String)]()
        }

        var sequencesPar = sequences.par
        val start: Long = System.nanoTime()
        var filteredPar = sequencesPar.filter( { case (sequence) => this.getMeanQuality(sequence._2, base) > threshold } )
        val duration: Float = (System.nanoTime()-start)/Constants.NanoInMillis
        
        if (verbose) println(f"Filtered by mean quality in parallel in $duration ms")
        return filteredPar.toArray
    }


    /** Filter sequences in chosen manner
     *  Get only kmers with mean Phred quality score not worse than given threshold
     *  Return an array containing filtered kmers
     */
    def filterByMeanQuality(sequences: Array[(String, String)],
                            threshold: Integer = Constants.PhreadDefaultThreshold,
                            base: Integer,
                            parallelMode: Boolean = false,
                            verbose: Boolean = false): Array[(String, String)] = {
        if (parallelMode) return this.filterByMeanQualityParallel(sequences, threshold, base, verbose)
        else return this.filterByMeanQualitySequential(sequences, threshold, base, verbose)
    }
}
