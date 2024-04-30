package bio.utils



object ClusterUtils {
    /*  Divide given array of substrings into clusters
     */
    def makeBaseClusters(substrings: Array[String],
                        position: Integer = 0): Array[Array[String]] = {
        var clusters = Map[Char, Array[String]]()
        val basesCluster = substrings.groupBy(_.charAt(position).toUpper)

        val aCluster = basesCluster.get('A').get
        val cCluster = basesCluster.get('C').get
        val gCluster = basesCluster.get('G').get
        val tCluster = basesCluster.get('T').get

        Array(aCluster, cCluster, gCluster, tCluster)
    }


    /* Get GC-content for given sequences
     */
    def getContentFeature(data: Array[String]): Array[Float] = {
        data.map(str => MetricsUtils.getGcContent(str))
    }

    
    /* Get frequency of each base in given sequences
     */
    def getFrequencyFeature(data: Array[String]): Array[Array[Float]] = {
        val aContent = data.map( str => MetricsUtils.getBasesFrequency('A', str) )
        val cContent = data.map( str => MetricsUtils.getBasesFrequency('C', str) )
        val gContent = data.map( str => MetricsUtils.getBasesFrequency('G', str) )
        val tContent = data.map( str => MetricsUtils.getBasesFrequency('T', str) )

        Array(aContent, cContent, gContent, tContent)
    }

}
