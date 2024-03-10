package types

import scala.collection.mutable.ArrayBuffer

object Biotype {
    type Alignment = Array[String]
    type CurrentAlignment = ArrayBuffer[String]
    type Population = Array[Alignment]
    type CurrentPopulation = ArrayBuffer[Alignment]

    type PairwiseAlignment = (String, String)
    type SubstitutionMatrix = Array[Array[Int]]
}
