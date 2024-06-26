package bio.align.multiple

/* External imports */
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/* Internal imports */
import types.Biotype.{Alignment, CurrentPopulation}



object Utils {


    /* Make all sequences in alignment the same length
    */
    def adjustAlignment(alignment: Alignment): Alignment = {
        val adjusted: ArrayBuffer[String] = new ArrayBuffer[String]()
        val maxLength = alignment.maxBy(_.length).length

        for (sequence <- alignment) {
            adjusted += sequence.padTo(maxLength, '-')
        }

        adjusted.toArray
    }


    /* Perform cyclic shift for the single sequence in an alignment
    */
    def cyclicShift(specimen: Alignment): Alignment = {
        val sequenceId: Int = Random.nextInt(specimen.length)
        val shifts: Int = Random.nextInt(specimen.head.length)

        val mutant: ArrayBuffer[String] = specimen.clone().to[ArrayBuffer]
        mutant(sequenceId) = mutant(sequenceId).takeRight(shifts) + mutant(sequenceId).take(mutant(sequenceId).length - shifts)
        mutant.toArray
    }


    /* Check average length of sequences in a given set
    *  This method is used to decide whether more gaps should be added during mutation
    */
    def getAverageLength(sequences: CurrentPopulation): Int = {
        val totalLength: Int = sequences.flatten.map(_.length).sum
        val numberOfSequences: Int = sequences.flatMap(_.toList).length
        totalLength/numberOfSequences
    }


    /* Get random samples from array of integers
    */
    def randomSample(array: Array[Int],
                     numberOfSamples: Int): Array[Int] = {
        val samples: ArrayBuffer[Int] = new ArrayBuffer[Int]()
        if (array.length < numberOfSamples) return samples.toArray

        val choices = Random.shuffle(array.indices.toList).take(numberOfSamples)
        choices.map(array(_)).toArray
    }
}
