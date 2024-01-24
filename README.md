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
According to information from [Apache Spark documentation](https://spark.apache.org/docs/latest/spark-standalone.html):

"_The launch scripts do not currently support Windows. To run a Spark cluster on Windows, start the master and workers by hand._"


To create a standalone cluster on Windows, use:
```bash
# Start master node
spark-class org.apache.spark.deploy.master.Master

# Start worker node
spark-class org.apache.spark.deploy.worker.Worker <master-URL>
```

## Usage

## References