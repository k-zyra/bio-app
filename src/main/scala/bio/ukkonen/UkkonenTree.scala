package bio.ukkonen

import java.util.Arrays
import java.util.Arrays._
import java.util.Collections
import scala.collection.mutable

import utils.Constants
import utils.Logger


class UkkonenTree {
    val root: UkkonenNode = Constants.ROOT
    val linker: UkkonenLinker = new UkkonenLinker
    var nodes: Array[UkkonenNode] = Array(root)
    var edges: Array[UkkonenEdge] = Array[UkkonenEdge]()
    var links: Array[UkkonenLink] = Array[UkkonenLink]()

    val logger = new Logger("UkkonenTree")

    def createNode(id: Integer = nodes.length): UkkonenNode = {
        logger.logInfo(s"Adding new node with id: $id")

        var newNode = new UkkonenNode(id)
        nodes :+= newNode
        return newNode
    }

    def createEdge(activePoint: Integer): UkkonenEdge = {
        logger.logInfo(s"Adding new edge, which starts at: $activePoint")

        var newEdge = new UkkonenEdge(activePoint)
        edges :+= newEdge
        return newEdge
    }

    def createSuffix(activePoint: Integer): UkkonenEdge = {
        logger.logInfo(s"Adding new suffix which starts at: $activePoint")

        var newEdge = this.createEdge(activePoint)
        var newNode = this.createNode()   
        // var newNode = this.createNode(activePoint)   
        newEdge.setDestination(newNode.id)
        println("Setting destination...")
        return newEdge
    }

    /** Splits an edge and inserts new node
     * 
     * During this operation, two new nodes are created:
     *  insertedNode - node created in the middle of edge
     *  leafNode - node created at the end of the one of the edges 
     */
    // def splitNode(node: UkkonenNode, startFirst: Integer, startSecond: Integer): Unit = {
    def splitNode(nodeId: Integer, startFirst: Integer, startSecond: Integer): Unit = {
        var insertedNode = this.createNode(this.nodes.length)
        var secondNode = this.createNode(this.nodes.length)

        var firstEdge = this.createEdge(startFirst)
        var secondEdge = this.createEdge(startSecond)

        println("Setting destination for edges after split...")
        firstEdge.setDestination(nodeId)
        secondEdge.setDestination(secondNode.id)

        insertedNode.addOutEdges(Array(firstEdge, secondEdge))

        var link = this.linker.link(insertedNode.id)
        if (link != None) links :+= link.get

        logger.logInfo(s"Adding new edges, first: $firstEdge, second: $secondEdge")
    }

    def getNumberOfEdges(): Integer = {
        return edges.length
    }

    def getNumberOfNodes(): Integer = {
        return nodes.length
    }

    def getNumberOfLinks(): Integer = {
        return links.length
    }

    def getWidth(): Integer = {
        return edges.length
    }

    def displayEdges(): Unit = {    
        edges.foreach(println)
    }

    def displayNodes(): Unit = {
        nodes.foreach(println)
    }

    def displayLinks(): Unit = {
        links.foreach(println)
    }

    def summary(): Unit = {
        println("=======================================================")
        println("Number of edges: " + this.getNumberOfEdges())
        println("Number of nodes: " + this.getNumberOfNodes())
        println("Number of links: " + this.getNumberOfLinks())
        println("=======================================================")
    }

    def showTree(): Unit = {
        for(node <- this.nodes) {
            println("* Node " + node.id)
            // println("Number of edges: " + node.getNumberOfOutEdges())
            node.displayOutEdges()
        }

        this.displayLinks()
    }
}
