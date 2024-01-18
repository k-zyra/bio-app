package bio.datatypes

import org.apache.spark.rdd._
import org.apache.spark.SparkContext


class Sequence(_header: String, _read: String, _score: String) {
	val header: String = _header
	val read : String = _read
	val score: String = _score


	/** Display Sequence class header
     */
	override def toString(): String = {
		return s"@$header\n$read"
	}
}
