package bio.align.multiple

/* External imports */
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/* Internal imports */
import misc.Logger
import types.Biotype.Alignment


object BlockMutation {
    private val logger = new Logger("MSA_BlockMutation")


    /* Perform block shuffle for an alignment
    *  Gaps will be moved till they will be merged with different gaps
    */
    def blockShuffle(specimen: Alignment,
                     shifts: Int = 1): Unit = {

    }


    /* Modify single specimen by local realignment in a specific block
    */
    def localRealignment(specimen: Alignment): Alignment = {
        val sequenceLength: Int = specimen(0).length
        println(s"Sequence length: ${sequenceLength}")
        println(s"Max window size: ${(sequenceLength/4)+3}")
        val windowSize: Int = Random.nextInt(sequenceLength / 4) + 3
        val position: Int = Random.nextInt(sequenceLength - windowSize)
        val sequenceId: Int = Random.nextInt(specimen.length)
        val sequence: String = specimen(sequenceId)
        if (!sequence.contains('-')) return specimen

        val gapsIds: Array[Int] = GapMutation.getGapIndices(sequence).toArray

        println(s"Chosen sequence ${sequenceId}  : ${sequence}")
        println(s"Window size: ${windowSize}")
        println(s"Starting from: ${position}")
        println(s"Get window: ${sequence.substring(position, position+windowSize)}")
        println(s"Gap indices: ")
        gapsIds.foreach(println)

        for (i <- 0 to windowSize) {
            println(s"Considering ${i} relocation")
//            var newWindow: String = randomSequence.substring()
        }

        val child: ArrayBuffer[String] = new ArrayBuffer[String]()
        return child.result().toArray
    }


    /* Modify single specimen by moving horizontally reference sequence
    */
    def moveReferenceSequence(specimen: Alignment): Alignment = {
        val mutationPower: Int = Random.nextInt(5) + 1
        val shift: Int = Random.nextInt(Config.initialAverageLength/mutationPower) + 1
        val referenceShifted: String = ("-" * shift) + specimen.head

        println(s"Mutation power: ${mutationPower}")
        println(s"Shift: ${shift}")

        var mutant: Alignment = specimen.clone()
        mutant(0) = referenceShifted

        return Utils.adjustAlignment(mutant).toArray
    }
}
