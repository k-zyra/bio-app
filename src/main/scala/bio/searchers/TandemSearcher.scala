package bio.searchers

import java.util.Arrays._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types._
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.{Row, SparkSession}
import scala.util.Sorting._

import utils.Logger


object TandemSearcher {
	var logger: Logger = new Logger("TandemSearcher") 


	/**  Generate suffixes from given string 
     *   Returns list of tuples where first element is the suffix id and second is a suffix
     */
    def findTandems() : List[(Integer, Integer)] = {
      	var tandems = List[(Integer, Integer)]()
      	return tandems
    }


    /**  Estimate number of tandems 
     *   Briefly, but coarsely estimates number of tandems using suffix arrays
     */
    def fastTandemEstimate(): Integer = {
        return 0
    }
}
