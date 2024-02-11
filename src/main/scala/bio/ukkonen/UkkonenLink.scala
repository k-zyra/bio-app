package bio.ukkonen

import misc.{Constants, Logger}
import scala.Option


class UkkonenLink(src: Integer, dest: Integer) {
    var source: Integer = src
    var destination: Integer = dest

    override def toString() : String = {
        return s"$source -> $destination"
    }
}

class UkkonenLinker() {
    var startNode: Integer = Constants.NodeIndex 

    private var logger: Logger = new Logger("UkkonenLinker")

    def reset(): Unit = {
        this.startNode = Constants.NodeIndex
    }

    def saveNode(node: Integer): Unit = {
        this.startNode = node
    }

    def createLink(endNode: Integer): UkkonenLink = {
        logger.logInfo(s"Creating link: $startNode -> $endNode")
        return new UkkonenLink(this.startNode, endNode)
    }

    def link(node: Integer): Option[UkkonenLink] = {
        var maybeLink: Option[UkkonenLink] = None

        if (this.startNode == Constants.NodeIndex) {
            this.saveNode(node)
        } else {
            maybeLink = Some(this.createLink(node))
            this.reset()
        }

        return maybeLink
    }
}