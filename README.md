# bio-app

## General informations

## Table of contents
* [Prerequisites](#Prerequisites)
* [Usage](#Usage)
* [Configuration](#Configuration)
* [Monitoring](#Monitoring)
* [Troubleshooting](#Troubleshooting)
* [Examples](#Examples)
* [References](#References)

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

## Configuration
To be done.


## Monitoring

Information about running nodes are available in a browser on `<master-URL>`, which was displayed during starting the master node.
```bash
localhost:<master-port>
```
By default, it is binded to the port `localhost:8080`
____

To access Spark Web UI and display information about jobs, stages, storage, etc., open a browser and go to:
```bash
localhost:4040
```
If you have more than one application up at the same time, they are binded to the subsequent ports: `localhost:4041`, `localhost:4042`,
 and so on.

**Note:** this UI is available only when the application is running.
To restore UI from already finished applications, see [Monitoring and instrumentation](https://spark.apache.org/docs/3.0.0-preview/monitoring.html)
page.

____
Web UI for HDFS management is accesible via:
```bash
localhost:9870
```

## Troubleshooting
The following problems may occur while submitting the application to Apache Spark:

#### Issue 1 - Failed to load class [class name]

#### Solution
Ensure that correct class and package names are given as arguments to the `spark-submit` and that chosen class has `main` function implemented.

#### Issue 2 - Connection refused: localhost/[ip_addres]:[port] 
The most common reason for this issue is IP address mismatch between the value given in `SparkController.scala` while building SparkSession and the value of env variable `SPARK_LOCAL_IP` set in `${SPARK_HOME}/conf/spark-env.sh`


#### Issue 3 - Did not find winutils.exe (Windows only)


#### Solution
_Step 1._ Install `winutils.exe` file from a directory dedicated for used Apache Spark version from [this website](https://github.com/kontext-tech/winutils). <br>
_Step 2._ Create a directory `C:\\Program Files\Hadoop\bin` and place the `winutils.exe` file inside.

## Examples
To run one of the provided examples, build an application according to instructions above and use:
```bash
spark-submit --class "examples.<example-name>" --master <master-URL> <path-to-JAR>
```

## References
[Apache Spark documentation](https://spark.apache.org/docs/latest/spark-standalone.html)<br>
[sbt Reference Manual](https://www.scala-sbt.org/1.x/docs/index.html)<br>
[Scala documentation](https://docs.scala-lang.org/style/scaladoc.html)


##
[Return to the top](#bio-app)
