package bio.ukkonen

import utils.Constants


class UkkonenEdge(var start: Integer = Constants.EDGE_START, var end: Integer = Constants.EDGE_END, 
                  var dest: Integer = Constants.NODE_INDEX) {
    var startIndex: Integer = start
    var endIndex: Integer = end
    var destination: Integer = dest

    override def toString() : String = {
        return s"Edge [$startIndex, $endIndex] -> ($destination)"
    }

    def setEndIndex(activeLength: Integer): Unit = {
        this.endIndex = this.startIndex + activeLength - 1
    }

    def getDestination(): Integer = {
        return destination
    }

    def setDestination(newDestination : Integer): Unit = {
        destination = newDestination
    }
}
