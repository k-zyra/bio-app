# bio-app

## General informations

## Prerequisites

## Building an application

To build an application, the Scala Build Tool should be installed.
Check the version of installed SBT by command:

```bash
sbt --version
```

Later, go to the project's main directory and type: 

```bash
sbt assembly
```


## Deploying an application to Apache Spark
### Linux

To create a standalone cluster on Linux, use:
```bash
# Start master node
./start-master.sh

# Start worker node
./start-slave.sh <master-URL>
```


### Windows
According to [Apache Spark documentation](https://spark.apache.org/docs/latest/spark-standalone.html):

"_The launch scripts do not currently support Windows.<br> To run a Spark cluster on Windows, start the master and workers by hand._"


To create a standalone cluster on Windows, use:
```bash
# Start master node
spark-class org.apache.spark.deploy.master.Master

# Start worker node
spark-class org.apache.spark.deploy.worker.Worker <master-URL>
```

## Usage
To run an application using Apache Spark, use `spark-submit` script:
```bash
spark-submit --class "app.BioApp" --master <master-URL> <path-to-JAR>
```

Additional flags which could be useful while running an application:
```
--verbose, -v - enable debug output
--total-executor-cores [NUM] - total number of executors
--executor-cores [NUM] - number of cores used by each executor
```
For other options, see `spark-submit --help`.

## References
[Apache Spark documentation](https://spark.apache.org/docs/latest/spark-standalone.html)<br>
[sbt Reference Manual](https://www.scala-sbt.org/1.x/docs/index.html)