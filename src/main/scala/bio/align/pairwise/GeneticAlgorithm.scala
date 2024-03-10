package bio.align.pairwise

/* External imports */
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/* Internal imports */
import app.SparkController
import misc.{Constants, Logger}
import types.Biotype


object PwaGeneticAlgorithm {
    private var generationSize: Integer = 0

    private var logger = new Logger("PWA_GeneticAlgorithm")
    

    /* Generate an initial generation G0 for the genetic algorithm 
    *
    * This method requires the following parameters:
    *   generation size - number of species in a generation
    *   sequence length - number of residues and gaps in sequences, all sequences are of the same length
    *   alignment size - number of sequences in a single alignment  
    * 
    *  Return array of alignments 
    */
    def generateInitialGeneration(generationSize: Integer,
                                firstSequence: String,
                                secondSequence: String,
                                verbose: Boolean = logger.isVerbose()): Unit = {
        this.generationSize = generationSize
        var generation: ArrayBuffer[Biotype.PairwiseAlignment] = ArrayBuffer[Biotype.PairwiseAlignment]()
        
        val start: Long = System.nanoTime()
        for (i <- 0 to generationSize) {
            println("i = " + i)
        }
        val duration: Float = (System.nanoTime() - start)/Constants.NanoInMillis

        if (verbose) logger.logInfo(f"Time spent in <generateInitialGeneration> ${duration} ms")
    }


    /*  
    */
    def getObjectiveFunction(): Unit = {

    }


    /* Modify single specimen  
    */
    def mutation(parent: Biotype.PairwiseAlignment,
                insertPosition: Integer,
                insertSequence: Integer): Unit = {
        
    }


    /*  
    */
    def evaluation(): Unit = {

    }


    /*  
    */
    def breeding(): Unit = {

    }


    /*  
    */
    def getAlignments(): Unit = {

    }


    /*  
    */
    def start(substitutionMatrix: Biotype.SubstitutionMatrix): Unit = {

    }
}