package bio.ukkonen

import scala.Option

import utils.Constants
import utils.Logger


class UkkonenLink(src: Integer, dest: Integer) {
    var source: Integer = src
    var destination: Integer = dest

    override def toString() : String = {
        return s"$source -> $destination"
    }
}

class UkkonenLinker() {
    var startNode: Integer = Constants.NODE_INDEX 

    private var logger: Logger = new Logger("UkkonenLinker")

    def reset(): Unit = {
        this.startNode = Constants.NODE_INDEX
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

        if (this.startNode == Constants.NODE_INDEX) {
            this.saveNode(node)
        } else {
            maybeLink = Some(this.createLink(node))
            this.reset()
        }

        return maybeLink
    }
}