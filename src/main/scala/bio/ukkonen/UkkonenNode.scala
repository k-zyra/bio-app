package bio.ukkonen

import java.util.Arrays._
import scala.collection.mutable

import utils.Constants


class UkkonenNode(var id: Integer = Constants.NodeIndex) {
    var index: Integer = id
    var outgoingEdges: Array[UkkonenEdge] = Array[UkkonenEdge]()

    override def toString(): String = {
        return s"Node $index"
    }

    def isLeaf(): Boolean = {
        return (outgoingEdges.length == 0)
    }
    
    def addOutgoingEdge(edge: UkkonenEdge): Unit = {
        outgoingEdges :+= edge
    }

    def addOutEdges(edges: Array[UkkonenEdge]): Unit = {
        edges.foreach(edge => this.outgoingEdges :+= edge)
    }

    def displayOutEdges(): Unit = {
        outgoingEdges.foreach(println)
    }

    def getOutLabels(): Array[UkkonenEdge] = {
        return outgoingEdges
    }

    def getNumberOfOutEdges(): Integer = {
        return outgoingEdges.length
    }

}
