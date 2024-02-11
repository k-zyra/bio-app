package misc

object LoggingLevel extends Enumeration {
    type LoggingLevel = Value
    val NONE, INFO, DEBUG, WARNING, ERROR, ALL = Value
}
