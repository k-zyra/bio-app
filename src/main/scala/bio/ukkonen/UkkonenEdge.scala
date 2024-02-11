package bio.ukkonen

import misc.Constants


class UkkonenEdge(var start: Integer = Constants.EdgeStart, var end: Integer = Constants.EdgeEnd, 
                  var dest: Integer = Constants.NodeIndex) {
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
