package bio.datatypes



class Sequence(_header: String, _read: String, _score: String) {
	val header: String = _header
	val read : String = _read
	val score: String = _score


	/** Display Sequence class header
     */
	override def toString(): String = {
		s"@$header\n$read"
	}
}
