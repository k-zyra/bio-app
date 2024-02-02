package utils

/* External imports */
import com.github.vickumar1981.stringdistance.StringDistance._

import java.util.Arrays._
import java.util.HashMap

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types._
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.{Row, SparkSession}

import scala.collection.immutable
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.{Map => MutableMap}
import scala.collection.mutable.StringBuilder

/* Internal imports */
import app.SparkController


object StringUtils {
    private val logger = new Logger("StringUtils")


    /**  Converts given String to Char
     *   If string is longer than 1, first character of a string is returned  
     */
    def toChar(string: String): Char = {
       return string(0).toChar
    }


    /**  Standardize whole string to certain (lower or upper) case
     */
    def standardize(str: String, upper: Boolean = true): String = {
        if (upper) return str.toUpperCase()
        else return str.toLowerCase()
    } 


    /**  Remove any single nucleotide repeat 
     *   Can be used only for number of repeats estimation. 
     */
    def simplify(seq: String): String = {
        var simplified: StringBuilder = new StringBuilder(Constants.EmptyString)
    
        for (n <- 1 to seq.length() - Constants.ArrayPadding) {
            if (seq(n-1) != seq(n)) simplified.append(seq(n-1))
        }
        simplified.append(seq.takeRight(1))
        return simplified.result()
    }


	/**  Check if given string has suffix format (ends with a sentinel)  
     */
    def hasSuffixFormat(str: String, sentinel: String = Constants.DefaultSentinel): Boolean = {
        if (str.endsWith(sentinel)) return true
        else return false
    }


    /**  Check if there is an overlap between two given strings
     *   Checks two possible orders of given substrings
     */
    def areOverlapping(firstStr: String, 
                       secondStr: String, 
                       overlapLength: Integer = Constants.ParameterUnspecified): Boolean = {
        if (firstStr.length() != secondStr.length()) {
            logger.logWarn("Given strings are different length. Cannot check overlap.")
            return false
        }

        var overlap: Integer = overlapLength
        if (overlap == Constants.ParameterUnspecified) overlap = firstStr.length()-1

        if (firstStr.takeRight(overlapLength) == secondStr.take(overlapLength)) {
            return true
        } else if (secondStr.takeRight(overlapLength) == firstStr.take(overlapLength)) {
            return true
        }

        return false
    }


    /**  Get exact match score 
     *   Return -1 if match not found 
     */
    def _exactMatch(firstStr: String,
                    secondStr: String,
                    overlapLength: Int): Int = {
        var len: Int = firstStr.length() - 1
        var score: Int = Constants.NotFoundInteger
        
        for (offset <- 0 to overlapLength) {
            println("offset: " + offset)
            println("first char: " + firstStr.charAt(len - offset))
            println("second char: " + secondStr.charAt(offset))
            if (firstStr.charAt(len - offset) != secondStr.charAt(offset)) {
                return score   
            } else score += 1
        }

        return score
    } 


    /**  Get estimated match score
     *   Return -1 if match not found 
     */
    def _estimatedMatch(firstStr: String, secondStr: String): Float = {
        var score: Float = Constants.NotFoundFloat

        if (firstStr.length() == secondStr.length()) {
            score = (Hamming.distance(firstStr, secondStr).toFloat/firstStr.length().toFloat)
        }
        return score
    }


    /**  Get best overlap between two strings
     *   Return the starting index for first and second strings saved as float
     *   If overlap (exact or not) not found, return -1 
     */
    def getBestOverlap(firstStr: String, 
                    secondStr: String, 
                    overlapLength: Int = Constants.ParameterUnspecified,
                    forceExact: Boolean = Constants.Force,
                    verbose: Boolean = logger.isVerbose()): (String, Int) = {
        var bestOverlap: (String, Int) = (Constants.EmptyString, Constants.NotFoundInteger)
        var overlapLen: Int = overlapLength

        if (overlapLen == Constants.ParameterUnspecified) {
            overlapLen = firstStr.length - 1
            if (verbose) logger.logInfo(s"Parameter [overlapLength] not specified. Value set to $overlapLen")
        } 

        if (forceExact) {
            var score = this._exactMatch(firstStr, secondStr, overlapLen)
            // var keepSearching: Boolean = true
            // var overlapFound: Integer = Constants.NOT_FOUND_I
            // overlapFound = 

            // while (keepSearching) {
            //     overlapFound = this._exactMatch(firstStr.takeRight(overlapLen), secondStr.take(overlapLen))
            //     if (overlapFound > bestOverlap._2) {
            //         bestOverlap = (secondStr.take(overlapLen), overlapLen)
            //         keepSearching = false
            //     }

            //     overlapLen -= 1
            //     if (overlapLen < Constants.ZERO_REMAINING) {
            //         keepSearching = false
            //     }
            // }
        } else {
            var overlapFound: Float = Constants.NotFoundFloat
            this._estimatedMatch(firstStr, secondStr)
        }

        return bestOverlap
    }

    
    /**  Find overlap candidates in SA for given string
     */
    def findOverlapCandidates(str: String,
                    kmers: Seq[String],
                    overlapLength: Int = Constants.ParameterUnspecified,
                    verbose: Boolean = logger.isVerbose()): Array[String] = {
        var overlapLen: Int = overlapLength
        if (overlapLen == Constants.ParameterUnspecified || overlapLen >= str.length()) {
            overlapLen = str.length() - 1
        }

        var prefix: String = str.takeRight(overlapLen)
        var candidates = kmers.filter(_.startsWith(prefix))

        if (verbose) logger.logInfo(s"Found ${candidates.length} candidates for overlaps.")
        return candidates.toArray
    }


    /**  Get length of longest common prefix for pair of strings
     *   Return -1 if strings do not have a common prefix
     */
    def getLengthOfLongestCommonPrefix(firstStr: String, secondStr: String): Integer = {
        var id: Integer = 0
        val maxPrefixLen: Integer = Math.min(firstStr.length, secondStr.length)
        
        while (id < maxPrefixLen && firstStr.charAt(id) == secondStr.charAt(id))  id += 1
        return id
    } 


    /**  Get longest common prefix for pair of strings
     *   Return an empty string if strings do not have a common prefix
     */
    def getLongestCommonPrefix(firstStr: String, secondStr: String): String = {
        var id: Integer = this.getLengthOfLongestCommonPrefix(firstStr, secondStr)
        return firstStr.substring(0, id)
    }


    /**  Find all suffixes from SA which share a common prefix with given suffix
     *   Return a Sequence of suffixes and their starting indexes 
     */
    def getCommonPrefix(suffix: String, sa: Seq[(Int, String)]): Seq[(Int, String)] = {
        var suffixId: Integer = sa.indexWhere(suf => suf._2 == suffix)        
        if (suffixId == sa.length - Constants.ArrayPadding) return Seq()

        var prefix: String = this.getLongestCommonPrefix(sa(suffixId)._2, sa(suffixId + Constants.ArrayPadding)._2) 
        if (prefix == Constants.EmptyString) return Seq()

        var suffixes = sa.filter(x => x._2.startsWith(prefix))
        return suffixes
    }


    /**  Find all suffixes from SA which starts with a certain substring
     *   Return a Sequence of suffixes and their starting indexes 
     */
    def havingCommonPrefix(prefix: String, sa: Seq[(Int, String)]): Seq[(Int, String)] = {
        var suffixes = sa.filter(x => x._2.startsWith(prefix))
        return suffixes
    }


    /**  Get all suffixes from SA which share a common prefix with given suffix
     *   Return a Sequence of suffixes and their starting indexes 
     */
    def getAllWithCommonPrefix(suffix: String, sa: Seq[(Int, String)]): Seq[(Int, String)] = {
        var id: Integer = sa.indexWhere(suf => suf._2 == suffix)        
        if (id == sa.length - Constants.ArrayPadding) return Seq()

        var prefix: String = this.getLongestCommonPrefix(sa(id)._2, sa(id + Constants.ArrayPadding)._2) 
        if (prefix == Constants.EmptyString) return Seq()

        var suffixes = sa.filter(x => x._2.startsWith(prefix))
        return suffixes
    }

    
    /**  Get all suffixes from SA which share a common prefix with given suffix
     *   Return a Sequence of suffixes and their starting indexes 
     */ 
    def getAllWithCommonPrefix(suffixId: Integer, sa: Seq[(Int, String)]): Seq[(Int, String)] = {
        var id: Integer = sa.indexWhere(suf => suf._1 == suffixId)
        if (id == sa.length - Constants.ArrayPadding) return Seq()

        var prefix: String = this.getLongestCommonPrefix(sa(id)._2, sa(id + Constants.ArrayPadding)._2) 
        if (prefix == Constants.EmptyString) return Seq()

        var suffixes = sa.filter(x => x._2.startsWith(prefix))
        return suffixes
    }


    /** Check whether given suffix contains a repeated substring in SA.
    *   Return a boolean indicator.
     */
    def hasRepeatedSubstring(suffix: String, sa: Seq[(Int, String)]): Boolean = {
        var numberOfSuffixes = this.getAllWithCommonPrefix(suffix, sa).length
        if (numberOfSuffixes > 1) return true
        else return false
    }


    /** Check whether suffix (specified by ID) contains a repeated substring in SA.
    *   Return a boolean indicator.
     */
    def hasRepeatedSubstring(prefixId: Integer, sa: Seq[(Int, String)]): Boolean = {
        var numberOfSuffixes = this.getAllWithCommonPrefix(prefixId, sa).length
        if (numberOfSuffixes > 1) return true
        else return false
    }


    /** Get repeated substring for given suffix from SA.
    *   Return LCP which occurs more than once in SA.
     */
    def getRepeatedSubstring(suffix: String, sa: Seq[(Int, String)]): String = {
        var id: Integer = sa.indexWhere(suf => suf._2 == suffix)        
        if (id == sa.length - Constants.ArrayPadding) return Constants.EmptyString

        var prefix: String = this.getLongestCommonPrefix(sa(id)._2, sa(id + Constants.ArrayPadding)._2) 
        if (prefix == Constants.EmptyString) return Constants.EmptyString
    
        var repeats: Integer = sa.count(x => x._2.startsWith(prefix))
        if (repeats > 1) return prefix
        else return Constants.EmptyString 
    }


    /** Get repeated substring for suffix (specified by ID) from SA.
    *   Return LCP which occurs more than once in SA.
     */
    def getRepeatedSubstring(suffixId: Integer, sa: Seq[(Int, String)]): String = {
        var id: Integer = sa.indexWhere(suf => suf._1 == suffixId)
        if (id == sa.length - Constants.ArrayPadding) return Constants.EmptyString

        var prefix: String = this.getLongestCommonPrefix(sa(id)._2, sa(id + Constants.ArrayPadding)._2) 
        if (prefix == Constants.EmptyString) return Constants.EmptyString

        var repeats: Integer = sa.count(x => x._2.startsWith(prefix))
        if (repeats > 1) return prefix
        else return Constants.EmptyString 
    }


    /** Find all tandem repeats from given SA.
    *   Return an array containing substring which occurs more than once.
     */
    def getTandemRepeats(sa: Seq[(Int, String)], verbose: Boolean = logger.isVerbose()): Array[String] = {
        var tandems = Array[String]()

        val start: Long = System.nanoTime()
		sa.foreach(suffix => tandems :+= StringUtils.getRepeatedSubstring(suffix._2, sa))
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) logger.logInfo(f"Tandem repeats found in $duration ms")
		return tandems.distinct.filter(element => element != Constants.EmptyString)
    }


    /** Find all tandem repeats from given SA.
    *   Return an array containing substring which occurs more than once.
    *   Result could by filtered to show tandems which occurs more than threshold times.
     */
    def countTandemRepeats(sa: Seq[(Int, String)],
                        threshold: Integer = Constants.MinThreshold, 
                        verbose: Boolean = logger.isVerbose()): Array[(String, Int)] = {
        val context = SparkController.getContext()
        var tandems = Array[String]()

        val start: Long = System.nanoTime()
		sa.foreach(suffix => tandems :+= StringUtils.getRepeatedSubstring(suffix._2, sa))

        val rddTandems = context.parallelize(tandems).map(tandem => (tandem, 1))
        val countedTandems = rddTandems.reduceByKey(_ + _) 
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) logger.logInfo(f"Tandem repeats found in $duration ms")
		return countedTandems.filter(tandem => 
                tandem._1 != Constants.EmptyString && 
                tandem._2 > threshold)
            .collect()
    }


    /** Find in the array first suffix starting with given prefix
    *   Return -1 if no such suffix found.
     */
    def findSuffix(arr: Array[String], prefix: String): Int = {
        var low: Integer = 0;
        var high: Integer = arr.length - Constants.ArrayPadding;

        while (low <= high) {
            var mid: Integer = (low + high) / 2;
            var currentString: String = arr(mid);

            if (currentString.startsWith(prefix)) {
                return mid; 
            } else if (currentString.compareTo(prefix) < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return Constants.IndexNotFound;
    }


    /**  Perofrm cyclic rotation on a string.
     *   Create an array with strings received from cyclic rotation.
     */
	def cyclicRotate(str: String, 
                    sentinel: String = Constants.DefaultSentinel,
                    verbose: Boolean = logger.isVerbose()): Array[String] = {
        val strBuilder = new StringBuilder(str)                
        if (!hasSuffixFormat(str, sentinel)) {
            if (verbose) logger.logWarn(f"Given string does not have a correct suffix format! Appending sentinel: $sentinel")    
            strBuilder.append(sentinel)
        } 

        val start: Long = System.nanoTime()
        var strToRotate = strBuilder.result()
		var strLength = strToRotate.length()
        var rotations = Array[String]()
		
        for (n <- 0 to strLength-Constants.ArrayPadding) {
			rotations :+= (strToRotate.slice(strLength-n, strLength) + strToRotate.slice(0, strLength-n)) 
		}

        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis
        if (verbose) logger.logInfo(f"Performed cyclic rotate for $strLength suffixes in $duration ms")
		return rotations
	}


    /**  Perform Burrows-Wheeler transform.
     *   Returns only last column from array created during cyclic rotation.
     */
	def burrowsWheelerTransform(str: String, verbose: Boolean = logger.isVerbose()): String = {
        var strToTransform: String = StringUtils.standardize(str)
		var transform: StringBuilder = new StringBuilder(Constants.EmptyString)
		var rotations: Array[String] = this.cyclicRotate(strToTransform, verbose = verbose)        
        
        rotations.sorted.foreach(substr => transform ++= substr.takeRight(1))
		return transform.result()
	}


    /**  Get first column of BW transform for inverting operation.
     *   Internal use only.
     */
    private def _getFirstColumn(bwt: String): Array[(Char, Int)] = {
        val countedRdd = MetricsUtils.countBases(bwt, verbose=false)
        return countedRdd.collect().sortBy(_._1)
    }


    /**  Get last column of BW transform for inverting operation.
     *   Internal use only.
     */
    private def _getLastColumn(bwt: String): Array[Int] = {
        var lastColumn = ArrayBuffer[Int]()
        var counters: MutableMap[Char, Int] = MutableMap('A' -> 0, 'C' -> 0, 'G' -> 0, 'T' -> 0, '$' -> 0)

        for (char <- bwt) {
            lastColumn += counters.apply(char)
            counters(char) += 1
        }
    
        return lastColumn.toArray
    }


    /**  Perform inversion of Burrows-Wheeler transform 
     *   Returns the original sequence from given transform
     */
    def inverseBurrowsWheeler(transform: String, 
                            sentinel: String = Constants.DefaultSentinel,
                            verbose: Boolean = logger.isVerbose()): String = {
        var strToInvert: String = this.standardize(transform)
        var inversedBwt = new StringBuilder
        val firstColumn = this._getFirstColumn(strToInvert).toMap
        val lastColumn = this._getLastColumn(strToInvert)

        val numberOfA: Integer = firstColumn.get('A').getOrElse(0).toInt
        val numberOfC: Integer = firstColumn.get('C').getOrElse(0).toInt
        val numberOfG: Integer = firstColumn.get('G').getOrElse(0).toInt
        val numberOfT: Integer = firstColumn.get('T').getOrElse(0).toInt

        var nextId: Int = 0
        var char = strToInvert.charAt(nextId)
        
        val start: Long = System.nanoTime()
        while (char != '$') {
            char match {
                case 'A' => {
                    nextId = Constants.StringOffset + lastColumn(nextId)
                }
                case 'C' => {
                    nextId = Constants.StringOffset + numberOfA + lastColumn(nextId)
                }
                case 'G' => {
                    nextId = Constants.StringOffset + numberOfA + numberOfC + lastColumn(nextId)
                }
                case 'T' => {
                    nextId = Constants.StringOffset + numberOfA + numberOfC + numberOfG + lastColumn(nextId)
                }
            }

            inversedBwt.insert(0, char)
            char = strToInvert.charAt(nextId)
        }
        inversedBwt.append(sentinel)
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) logger.logInfo(f"Time spent in <inverseBurrowsWheeler>: $duration ms")
        return inversedBwt.result()
    }

}