package misc

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Logger(name: String) {

    import LoggingLevel._

    val objectName: String = name
    var logHeader: String = s"$name"
    var debugHeader: String = s"[DEBUG] - $name"
    var infoHeader: String = s"[INFO] - $name"
    var warnHeader: String = s"[WARN] - $name"
    var errorHeader: String = s"[ERROR] - $name"
    var criticalHeader: String = s"[CRITICAL] - $name"

    var logLevel: LoggingLevel = LoggingLevel.WARNING
    var timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");


    override def toString(): String = {
        return s"$name set for logging level: $logLevel"
    }

    def logOn(): Unit = {
        logLevel = LoggingLevel.INFO
    }

    def logOff(): Unit = {
        logLevel = LoggingLevel.NONE
    }

    def isVerbose(): Boolean = {
        return (logLevel != LoggingLevel.NONE)
    }

    def getTimestamp(): String = {
        var timestamp: String = LocalDateTime.now().format(this.timeFormat)
        return s"$timestamp"
    }

    def logDebug(msg: String): Unit = {
        if (isVerbose()) {
            var now: String = this.getTimestamp()
            println(s"$now - $debugHeader: $msg")
        }
    }

    def logInfo(msg: String): Unit = {
        if (isVerbose()) {
            var now: String = this.getTimestamp()
            println(s"$now - $infoHeader: $msg")
        }
    }

    def logWarn(msg: String): Unit = {
        if (isVerbose()) {
            var now: String = this.getTimestamp()
            println(s"$now - $warnHeader: $msg")
        }
    }

    def logError(msg: String): Unit = {
        var now: String = this.getTimestamp()
        println(s"$now - $errorHeader: $msg")
    }

    def logCriticalError(msg: String): Unit = {
        var now: String = this.getTimestamp()
        println(s"$now - $criticalHeader: $msg")
    }

    def setLogHeader(header: String): Unit = {
        this.logHeader = header
    }

    def setErrorHeader(header: String): Unit = {
        this.errorHeader = header
    }

    def setDebugHeader(header: String): Unit = {
        this.debugHeader = header
    }

    def setInfoHeader(header: String): Unit = {
        this.infoHeader = header
    }

    def setLogLevel(level: LoggingLevel): Unit = {
        this.logLevel = level
    }

    def getLogLevel(): LoggingLevel = {
        return this.logLevel
    }
}
