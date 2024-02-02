package bio.ukkonen

import java.util.Arrays._
import scala.util.control.Breaks._

import utils.Constants
import utils.Logger


class UkkonenController {
    private var logger = new Logger("UkkonenController")

    private var activeEdge: Integer = Constants.DummyEdge
    private var activeNode: UkkonenNode = Constants.DummyNode
    private var activeLength: Integer = Constants.ZeroLength

    private var remainder: Integer = Constants.ZeroRemaining
    private var stepCounter: Integer = Constants.InitialStep
    private var currentString: String = Constants.EmptyString


    /**  Reset controller
     *   Do this when there is no link from inserted node
     */
    private def resetController(): Unit = {
        this.activeEdge = Constants.DummyEdge
        this.activeLength = Constants.ZeroLength
        this.activeNode = Constants.DummyNode

        this.remainder = Constants.ZeroRemaining
        this.stepCounter = Constants.InitialStep
        this.currentString = Constants.EmptyString
    }

     
    /**  Reset active point
     *   Do this when there is no link from inserted node
     */
    private def resetActivePoint(root: UkkonenNode, edge: Integer = Constants.DummyEdge): Unit = {
        this.activeLength -= 0 
        this.activeEdge = edge
        this.activeNode = root
    }

    
     /** Show info about active point
     *   Do this if there is a link from current activeNode
     */
    private def showActivePoint(): Unit = {
        println(
            s"(activeEge, activeLength, activeNode) = ($activeEdge, $activeLength, $activeNode)")
    }

    
     /** Follow link
     *   Do this if there is a link from current activeNode
     */
    private def followLink(tree: UkkonenTree, nodeId: Integer): Unit = {
        println("Following a link from node: " + nodeId)
        tree.displayLinks()

        var linkToFollow = tree.links.find(link => {link.source == nodeId})
        this.activeNode = tree.nodes(linkToFollow.get.destination)
    }

    
    /** Find in active node
     *  Find if there is a char in outgoing edges of active node (on active edge, with active position)
     */
    private def findInActiveNode(id: Integer): Integer = {
        var returnIndex: Integer = -1

        if (this.activeEdge != Constants.DummyEdge) {                              // active edge set
            println(s"Active edge exists, has start index: $activeEdge")

            var activeEdgeChar: Char = this.currentString(this.activeEdge)
            var pointerChar: Char = this.currentString(this.activeEdge + this.activeLength)
            println(s"active edge: $activeEdgeChar, pointer charL $pointerChar")

            if (this.currentString(this.activeLength + this.activeEdge) == this.currentString(id)) return this.activeEdge     
        } else {
            println("Active edge not set")
            for (edge <- this.activeNode.outgoingEdges) {
                if (this.currentString(id) == this.currentString(edge.start)) return edge.startIndex 
            }    
        }

        return returnIndex
    }


    /** Update active point
     *  If we found such character in edge outgoing from active point just update counter
     */
    private def updateActivePoint(i: Integer, tree: UkkonenTree): Unit = {
        if (activeEdge == Constants.DummyEdge) this.activeEdge = i
        this.activeLength += 1
        this.remainder += 1

        val edg = tree.root.outgoingEdges.find(edge => {edge.startIndex == this.activeEdge})
        println("While updating active point edge found: " + edg.get)

        var edgStart: Integer = edg.get.startIndex
        var edgEnd: Integer = edg.get.endIndex
        var destination: Integer = edg.get.destination

        println(s"**********************************************Edge found, start:  $edgStart stop: $edgEnd destination: $destination activeLen: $activeLength")
        if (edgStart + this.activeLength == edgEnd + 1) {
            println("should change active node!!!!!!!!!!!!!!!!!!!!!1")
            tree.displayNodes()
            var tryNode = tree.nodes(destination)
            println(tryNode.displayOutEdges())
            this.activeLength = 0
            this.activeNode = tryNode

            // this.activeLength = 0
            // this.activeNode = tree.nodes(destination)
            // this.showActivePoint()
            // this.followLink(tree, destination)
        }

        println(s"Updating active point - activeLen = $activeLength, activeEdge = $activeEdge")
    }


    /** Split node
     *  If active edge is set and we need to add new suffix
     */
    private def splitNode(i: Integer, tree: UkkonenTree): Unit = {
        var currentChar: Char = this.currentString(i)
        println(s"Splitting node, remainder: $remainder, current id: $i char: $currentChar")
        
        // Updating active edge
        val edgeeee = this.activeNode.outgoingEdges.find(edge => {edge.startIndex == this.activeEdge})
        edgeeee.get.setEndIndex(this.activeLength)
        println(s"updating edge destination: " + edgeeee.get.destination  + " -> " + tree.nodes.length)
        var bakap = tree.nodes.length
        
        tree.splitNode(edgeeee.get.destination, this.activeEdge+this.activeLength, i)
        edgeeee.get.setDestination(bakap)
        println("splitted edge after: ")
        println(edgeeee.get)

        // Splitting node
        this.remainder -= 1
        var testId = i-remainder

        if (this.remainder == 0) {
            println("should reset active point to the root")
            this.activeEdge = 0
            this.activeLength = 0
        } else {
            this.activeEdge += 1
            this.activeLength -= 1
            println("Active edge now: " + this.currentString(activeEdge))
            showActivePoint()
        }
    }


    /** Constructs Suffix Tree for given string using approach described by E. Ukkonen
     *  Returns created tree (it must be saved, controller does not manage any trees)
     */
    def constructTree(substr: String): UkkonenTree = {
        var numberOfSteps = substr.length
        var tree: UkkonenTree = new UkkonenTree()

        this.resetController()
        this.activeNode = tree.root
        this.currentString = substr + "$"
        println(currentString)
        
        for (i <- 0 to numberOfSteps) {
            println("")
            println("*************************")
            println("Considered character: " + this.currentString(i))

            var foundIndex = findInActiveNode(i)
            if (foundIndex != -1) {                             // If we found a character somewhere in a tree - just update counters
                this.updateActivePoint(foundIndex, tree)
            } else {                                            // Should add new suffixes
                 if (this.remainder == 0) {
                    tree.root.addOutgoingEdge(tree.createSuffix(i))
                } else {
                    // We will add new suffixes till remainder == 0 or we do not have a reason to split node
                    var keepSplitting: Boolean = true
                    while(keepSplitting) {
                        this.splitNode(i, tree)

                        if (this.remainder == 0) {
                            println("should reset active point to the root")
                            this.resetActivePoint(tree.root)
                            tree.root.addOutgoingEdge(tree.createSuffix(i))             // we reset to the root and add last element to the root
                            keepSplitting = false
                        }  

                        showActivePoint()
                    }                   
                }
            }

            println("*************************")
            println("")
        }

        return tree
    }

}
