package bio.clustering

/* External imports */
import org.apache.spark.ml.clustering.{KMeans, KMeansModel}
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

import scala.collection.mutable.ArrayBuffer

/* Internal imports */
import app.SparkController
import bio.utils.ClusterUtils
import misc.{Constants, Logger}


object KmeansCluster {
    private val logger = new Logger("KmeansCluster")

    private var assembler: Option[VectorAssembler] = None
    private var model: Option[KMeansModel] = None

    private var clusters: Option[DataFrame] = None
    private var aCluster = Array[String]()
    private var cCluster = Array[String]()
    private var gCluster = Array[String]()
    private var tCluster = Array[String]()


    /*  Get created clusters
     *  Clusters could be filtered by prediction class or string  
     *  Row with predictions will be truncated      
     */
    def getClusterAsArray(prediction: Option[Int] = None): Array[String] = {
        if (clusters.isEmpty) {
            logger.logCriticalError("No clusters exist.")
            return Array[String]()
        }
    
        var cluster: Option[DataFrame] = None
        if (prediction.isDefined) {
            cluster = Some(this.clusters.get.where(s"prediction == ${prediction.get}"))
        } else {
            cluster = Some(this.clusters.get)
        }

        if (!cluster.isDefined) {
            logger.logCriticalError("Error on retrieving cluster occured.")
            return Array[String]()
        }

        val arrayBuf = ArrayBuffer[String]()
        cluster.get.collect().foreach { row => 
            arrayBuf += row.getAs[String]("id")
        }

        arrayBuf.toArray
    }


    /*  Find cluster where given element is placed
     *  Return only cluster index, -1 if not found
     */
    def findCluster(element: String): Integer = {
        if (clusters.isEmpty) {
            logger.logCriticalError("No clusters exist.")
            return Constants.NotFoundInteger
        }

        val results = this.clusters.get.filter(col("id") === element)
        if (results.count() == 0) Constants.NotFoundInteger
        else results.first().getAs[Int]("prediction")
    }


    /*  Join element to an exisiting cluster using pre-trained model
     *  Return cluster index to which element is added
     */
    def joinToCluster(element: String, 
                    clusterType: Int = Constants.PrefixBased): Integer = {
        if (this.model == None || this.assembler == None) {
            logger.logCriticalError("Model do not exists yet.")
            return Constants.NotFoundInteger
        }

        // val features

        // val joinedData = this.assembler.get.transform()

        return 100
    }


    /*  Display created clusters in readible format
     *  Could be filtered by cluster index
     */
    def showClusters(rows: Integer = Constants.NumberOfRows,
                    filter: Option[Integer] = None): Unit = {
        var data: Option[DataFrame] = None
        if (filter.isDefined) {
            data = Some(clusters.get.where(s"prediction == ${filter.get}"))
        } else {
            data = Some(clusters.get)
        }

        if (!data.isDefined) {
            logger.logCriticalError("No cluster found.")
            return
        } else {
            data.get.show()
        }
    }


    /*  Display created clusters in readible format
     */
    private def _unpackData(elements: Array[String]): Boolean = {
        val clusters = ClusterUtils.makeBaseClusters(elements)
        if (clusters.isEmpty) return Constants.Failure

        this.aCluster = clusters(0)
        this.cCluster = clusters(1)
        this.gCluster = clusters(2)
        this.tCluster = clusters(3)

        Constants.Success
    }


    /*  Create clusters using k-means method
     *  Number of clusters can be specified using method argument, default 4
     */
    def createClusters(elements: Array[String],
                    clusters: Int = Constants.NumberOfClusters,
                    clusterType: Int = Constants.PrefixBased): Unit = {
        val isDataPrepared: Boolean = this._unpackData(elements)
        if (!isDataPrepared) {
            logger.logCriticalError("No data available to create clusters.")
            return
        }
        
        val featureValue = ClusterUtils.getContentFeature(aCluster)
        println(featureValue.getClass)

        val session = SparkController.getSession()
        val data: Seq[(String, Float)] = aCluster.zip(featureValue)
        val schema = List("id", "feature")
        val df = session.createDataFrame(data).toDF(schema: _*)

        val assembler = new VectorAssembler()
                            .setInputCols(Array("feature"))
                            .setOutputCol("features")
        this.assembler = Some(assembler)

        val assembledDF: DataFrame = assembler.transform(df)
        val k: Integer = clusters
        val kmeans = new KMeans()
                        .setK(k)
                        .setSeed(1L)

        val model = kmeans.fit(assembledDF)
        this.model = Some(model)

        val predictions = model.transform(assembledDF)
        this.clusters = Some(predictions)
    }
}
