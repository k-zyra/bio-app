package bio.clustering

/* External imports */
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.{SparkSession, DataFrame}

/* Internal imports */
import app.SparkController

import utils.ClusterUtils
import utils.Constants
import utils.Logger
import utils.MetricsUtils


object BasicCluster {
    var logger = new Logger("BasicCluster")

    var clusters = Map[Integer, Array[String]]()
    var aCluster = Array[String]()
    var cCluster = Array[String]()
    var gCluster = Array[String]()
    var tCluster = Array[String]()


    /*  Prepare data for clustering
     */
    private def _unpackData(elements: Array[String]): Boolean = {
        var clusters = ClusterUtils.makeBaseClusters(elements)
        if (clusters.isEmpty) return Constants.Failure

        this.aCluster = clusters(0)
        this.cCluster = clusters(1)
        this.gCluster = clusters(2)
        this.tCluster = clusters(3)

        return Constants.Success
    }

    
    /*  Divide given array of substrings into clusters
    *   Number of clusters can be given as a parameter
    */
    def createClusters(elements: Array[String],
                    clusters: Integer = Constants.NumberOfClusters): Unit = {
        val isDataPrepared: Boolean = this._unpackData(elements)
        if (!isDataPrepared) {
            logger.logCriticalError("No data available for create clusters.")
            return
        }
        
        if (clusters == Constants.NumberOfClusters) {
            logger.logInfo("Created base clusters")
            return
        }
        
        val featureValues = ClusterUtils.getContentFeature(this.aCluster)
        val data = aCluster zip featureValues
        println("Size of data: " + data.length)
        data.sortBy(_._2)
        // data.foreach(println)

        val maxV = data.head._2
        val minV = data.last._2
        // val maxV = data.tail._2

        println("Min val: " + minV)
        println("Max val: " + maxV)
        // data.groupBy(_._2 )
    }
}
