package utils

/* External imports */
import misc.{Constants, Logger}

import scala.collection.mutable.ArrayBuffer



object ScoreUtils {
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


    /**  Check whether given threshold for Phred quality score has correct value
     */
    def isThresholdCorrect(threshold: Integer): Boolean = {
        if (threshold < 0 || threshold > Constants.PhredMaxThreshold) {
            this.logger.logError(
                f"Incorrect value of threshold: ${threshold}, should be in range [0, ${Constants.PhredMaxThreshold}]")
            return false
        }
        return true
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
        if (!isThresholdCorrect(threshold)) {
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
        if (!isThresholdCorrect(threshold)) {
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


    /** Count correct (with quality equal or higher than threshold) base calls in a sequence
     */
    def countCorrectBaseCalls(sequence: String, 
                            threshold: Integer): Integer = {
        var counter: Integer = 0
        for (basecall <- sequence) {
            if (basecall.toInt > threshold) counter += 1
        }
        return counter
    }


    /** Filter sequences sequentially
     *  Get only sequences which contains appropriate number of base calls with satisfying Phred quality
     *  Return an array containing filtered sequences
     */
    def filterByNumberOfCorrectBaseCallSequential(sequences: Array[(String, String)],
                            threshold: Integer = Constants.PhreadDefaultThreshold,
                            expectedCounter: Integer,
                            base: Integer,
                            verbose: Boolean = false): Array[(String, String)] = {
        if (!isThresholdCorrect(threshold)) {
            return Array[(String, String)]()
        }
        var filtered: ArrayBuffer[(String, String)] = new ArrayBuffer[(String, String)]()

        val start: Long = System.nanoTime()
        for (sequence <- sequences) {
            if (this.countCorrectBaseCalls(sequence._2, threshold) >= expectedCounter) {
                filtered += sequence
            }
        }
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) println(f"Filtered by number of correct base calls in $duration ms")
        return filtered.result().toArray
    }


    /** Filter sequences in parallel manner
     *  Get only sequences which contains appropriate number of base calls with satisfying Phred quality
     *  Return an array containing filtered sequences
     */
    def filterByNumberOfCorrectBaseCallParallel(sequences: Array[(String, String)],
                            threshold: Integer = Constants.PhreadDefaultThreshold,
                            expectedCounter: Integer,
                            base: Integer,
                            verbose: Boolean = false): Array[(String, String)] = {
        if (!isThresholdCorrect(threshold)) {
            return Array[(String, String)]()
        }

        val sequencesPar = sequences.par
        val start: Long = System.nanoTime()
        var filtered = sequencesPar.filter( { case (sequence) => sequence._2.count( character => character>threshold ) > expectedCounter })
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) println(f"Filtered by number of correct base calls in $duration ms")
        return filtered.toArray
    }


    /** Filter sequences in chosen manner
     *  Get only kmers with mean Phred quality score not worse than given threshold
     *  Return an array containing filtered kmers
     */
    def filterByNumberOfCorrectBaseCall(sequences: Array[(String, String)],
                            threshold: Integer = Constants.PhreadDefaultThreshold,
                            expectedCounter: Integer,
                            base: Integer,
                            parallelMode: Boolean = false,
                            verbose: Boolean = false): Array[(String, String)] = {
        if (parallelMode) return this.filterByNumberOfCorrectBaseCallSequential(sequences, threshold, expectedCounter, base, verbose)
        else return this.filterByNumberOfCorrectBaseCallSequential(sequences, threshold, expectedCounter, base, verbose)
    }
}