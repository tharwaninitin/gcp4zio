# Gcp4zio
[![License](http://img.shields.io/:license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Tests](https://github.com/tharwaninitin/gcp4zio/actions/workflows/ci.yml/badge.svg)](https://github.com/tharwaninitin/gcp4zio/actions/workflows/ci.yml)

[//]: # ([![Semantic Versioning Policy Check]&#40;https://github.com/tharwaninitin/gcp4zio/actions/workflows/semver.yml/badge.svg&#41;]&#40;https://github.com/tharwaninitin/gcp4zio/actions/workflows/semver.yml&#41;)

**Gcp4zio** is simple Scala interface to Google Cloud API based on ZIO.

Add the latest release as a dependency to your project

| Module               | Latest Version                                                                                                                                                                                                   |                                                                                                                                                           Documentation | Scala Versions                                                                                                                                                                                                          | 
|----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Google Cloud Storage | [![Latest Version](https://maven-badges.herokuapp.com/maven-central/com.github.tharwaninitin/gcp4zio-gcs_2.12/badge.svg)](https://mvnrepository.com/artifact/com.github.tharwaninitin/gcp4zio-gcs)               |               [![javadoc](https://javadoc.io/badge2/com.github.tharwaninitin/gcp4zio-gcs_3/javadoc.svg)](https://javadoc.io/doc/com.github.tharwaninitin/gcp4zio-gcs_3) | [![gcp4zio-gcs Scala version support](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-gcs/latest-by-scala-version.svg)](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-gcs)                      |
| Dataproc             | [![Latest Version](https://maven-badges.herokuapp.com/maven-central/com.github.tharwaninitin/gcp4zio-dp_2.12/badge.svg)](https://mvnrepository.com/artifact/com.github.tharwaninitin/gcp4zio-dp)                 |                 [![javadoc](https://javadoc.io/badge2/com.github.tharwaninitin/gcp4zio-dp_3/javadoc.svg)](https://javadoc.io/doc/com.github.tharwaninitin/gcp4zio-dp_3) | [![gcp4zio-dp Scala version support](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-dp/latest-by-scala-version.svg)](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-dp)                         |
| BigQuery             | [![Latest Version](https://maven-badges.herokuapp.com/maven-central/com.github.tharwaninitin/gcp4zio-bq_2.12/badge.svg)](https://mvnrepository.com/artifact/com.github.tharwaninitin/gcp4zio-bq)                 |                 [![javadoc](https://javadoc.io/badge2/com.github.tharwaninitin/gcp4zio-bq_3/javadoc.svg)](https://javadoc.io/doc/com.github.tharwaninitin/gcp4zio-bq_3) | [![gcp4zio-bq Scala version support](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-bq/latest-by-scala-version.svg)](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-bq)                         |
| PubSub               | [![Latest Version](https://maven-badges.herokuapp.com/maven-central/com.github.tharwaninitin/gcp4zio-pubsub_2.12/badge.svg)](https://mvnrepository.com/artifact/com.github.tharwaninitin/gcp4zio-pubsub)         |         [![javadoc](https://javadoc.io/badge2/com.github.tharwaninitin/gcp4zio-pubsub_3/javadoc.svg)](https://javadoc.io/doc/com.github.tharwaninitin/gcp4zio-pubsub_3) | [![gcp4zio-pubsub Scala version support](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-pubsub/latest-by-scala-version.svg)](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-pubsub)             |
| Cloud Monitoring     | [![Latest Version](https://maven-badges.herokuapp.com/maven-central/com.github.tharwaninitin/gcp4zio-monitoring_2.12/badge.svg)](https://mvnrepository.com/artifact/com.github.tharwaninitin/gcp4zio-monitoring) | [![javadoc](https://javadoc.io/badge2/com.github.tharwaninitin/gcp4zio-monitoring_3/javadoc.svg)](https://javadoc.io/doc/com.github.tharwaninitin/gcp4zio-monitoring_3) | [![gcp4zio-monitoring Scala version support](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-monitoring/latest-by-scala-version.svg)](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-monitoring) |

This project is tested with scala versions @Scala213@, @Scala3@ and java versions @JavaVersions@

__SBT__
``` scala mdoc
libraryDependencies ++= List(
      "com.github.tharwaninitin" %% "gcp4zio-gcs" % "@VERSION@",
      "com.github.tharwaninitin" %% "gcp4zio-dp"  % "@VERSION@",
      "com.github.tharwaninitin" %% "gcp4zio-bq"  % "@VERSION@",
      "com.github.tharwaninitin" %% "gcp4zio-pubsub"  % "@VERSION@",
      "com.github.tharwaninitin" %% "gcp4zio-monitoring"  % "@VERSION@"
   )
```
__Maven__
```
<dependency>
    <groupId>com.github.tharwaninitin</groupId>
    <artifactId>gcp4zio-gcs_2.12</artifactId>
    <version>@VERSION@</version>
</dependency>
```
# GCP4ZIO API's
```shell
# To run all these examples set the GOOGLE_APPLICATION_CREDENTIALS environment variable to the location of the service account json key. 
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/key.json
```
<!-- TOC -->
- [GCP4ZIO API's](#gcp4zio-apis)
  - [Google Cloud Storage](#google-cloud-storage-api)
    - [CRUD Operations](#crud-operations)
    - [CRUD Operations (Streaming)](#crud-operations-streaming)
    - [Copy Objects from GCS to GCS](#copy-objects-from-gcs-to-gcs)
  - [Dataproc](#dataproc-api)
    - [Dataproc Cluster](#dataproc-cluster)
    - [Dataproc Job](#dataproc-job)
  - [Bigquery](#bigquery-api)
    - [Query/Read](#running-sql-queries-in-bigquery)
    - [Load/Export](#loadingexporting-data-fromto-gcs)
  - [PubSub](#pubsub-api)
    - [Topic](#topic)
    - [Subscription](#subscription)
    - [Publisher](#publisher)
    - [Subscriber (Streaming)](#subscriber)
  - [Monitoring](#monitoring-api)
<!-- /TOC -->

## Google Cloud Storage API
### CRUD Operations
```scala mdoc:silent
import gcp4zio.gcs._
import java.nio.file.Paths
import zio._

// Upload single object from local to provided bucket at provided prefix
val localPath1 = Paths.get("/local/path/to/file1.csv")
GCS.putObject("targetBucket", "temp/gcs/prefix/file1.csv", localPath1)

// Download single object from bucket at provided prefix to local
val localPath2 = Paths.get("/local/path/to/file2.csv")
GCS.getObject("srcBucket", "temp/gcs/prefix/file1.csv", localPath2)

// Delete single object from bucket at provided prefix
GCS.deleteObject("gcsBucket", "temp/gcs/prefix/file1.csv")
```
### CRUD Operations (Streaming)
```scala mdoc:silent
import gcp4zio.gcs._
import com.google.cloud.storage.Storage.BlobWriteOption

val src = GCS.getObject("gcsBucket", "temp/gcs/prefix/file1.csv", 4096)

val opts = List(BlobWriteOption.doesNotExist())
val sink = GCS.putObject("gcsBucket", "temp/test/ratings2.csv", opts)

src.run(sink)
```
### Copy Objects from GCS to GCS
```scala mdoc:silent
import gcp4zio.gcs._

// Copy single object from source bucket to target bucket
GCS.copyObjectsGCStoGCS(
  srcBucket = "srcBucket",
  srcPrefix = Some("temp/gcs/prefix/file1.csv"),
  targetBucket = "targetBucket",
  targetPrefix = Some("temp2/gcs/prefix/file1.csv")
)

// Copy all objects from source bucket to target bucket
GCS.copyObjectsGCStoGCS(
  srcBucket = "srcBucket",
  targetBucket = "targetBucket"
)

// Copy all objects from source bucket with prefix to target bucket
GCS.copyObjectsGCStoGCS(
  srcBucket = "srcBucket",
  srcPrefix = Some("temp/gcs/prefix"),
  targetBucket = "targetBucket"
)
```  

## Dataproc API
### Dataproc Cluster
```scala mdoc:silent
import gcp4zio.dp._

// Create Dataproc Cluster Properties
val props = new ClusterProps("dataproc-logs")
// Create Dataproc Cluster
val createTask = DPCluster.createDataproc("cluster1", props)

// Delete Dataproc Cluster
val deleteTask = DPCluster.deleteDataproc("dpCluster")

(createTask *> deleteTask).provide(DPCluster.live("gcpProject", "gcpRegion"))
```  

### Dataproc Job
```scala mdoc:silent
import gcp4zio.dp._

val libs = List("file:///usr/lib/spark/examples/jars/spark-examples.jar")
val conf = Map("spark.executor.memory" -> "1g", "spark.driver.memory" -> "1g")
val mainClass = "org.apache.spark.examples.SparkPi"

val job = DPJob.executeSparkJob(List("1000"), "mainClass", libs, conf)

job.provide(DPJob.live("dpCluster", "gcpProject", "gcpRegion", "dpEndpoint"))
```  

## Bigquery API
### Running SQL Queries in BigQuery
```scala mdoc:silent
import gcp4zio.bq._

// Execute DML/DDL query on Bigquery
val task1: RIO[BQ, Unit] = BQ.executeQuery("CREATE TABLE dataset1.test1 (column1 STRING)").unit

val task2: RIO[BQ, Unit] = BQ.executeQuery(""" INSERT INTO dataset1.test1 VALUES ("value1") """).unit

// Fetching data from Bigquery
val task3: RIO[BQ, Iterable[String]] = BQ.fetchResults("SELECT * FROM dataset1.test1")(rs => rs.get("column1").getStringValue)

(task1 *> task2 *> task3).provide(BQ.live())
```  
### Loading/Exporting data from/to GCS
```scala mdoc:silent
import gcp4zio.bq._
import gcp4zio.bq.FileType.PARQUET

// Load PARQUET file into Bigquery
val step = BQ.loadTable("inputFilePathParquet", PARQUET, Some("gcpProject"), "outputDataset", "outputTable")

step.provide(BQ.live())
```  

## PubSub API
### Topic
```scala mdoc:silent
import gcp4zio.pubsub.topic._

// Create PubSub Topic
PSTopic.createTopic(project = "gcsProjectId", topic = "topicName")

// Add IAM Policy Binding to existing Topic, where you grant basic pubsub role to a member  
PSTopic.addIAMPolicyBindingToTopic(project = "gcsProjectId", topic = "topicName", member = "example@principalAccount.com", role = "roles/<IAM_Role>")

// Delete PubSub Topic
PSTopic.deleteTopic(project = "gcsProjectId", topic = "topicName")
```
### Subscription
```scala mdoc:silent
import gcp4zio.pubsub.subscription._

// Create Pull Subscription
PSSubscription.createPullSubscription(
    project = "gcsProjectId", 
    subscription = "subName", 
    topic = "topicName",
    ackDeadlineSeconds = 20 // default 10 seconds
  )

// Create Push Subscription
PSSubscription.createPushSubscription(
    project = "gcsProjectId",
    subscription = "subName",
    topic = "topicName",
    ackDeadlineSeconds = 20, // default 10 seconds
    pushEndpoint = "https://example.com/push"
  )

// Create Bigquery Subscription
PSSubscription.createBQSubscription(
    project = "gcsProjectId",
    subscription = "subName",
    topic = "topicName",
    bqTableId = "projectId:datasetId.tableId"
  )

// Delete Subscription
PSSubscription.deleteSubscription(
  project = "gcsProjectId",
  subscription = "subName"
)
```
### Publisher
```scala mdoc:silent
import gcp4zio.pubsub.publisher._

// Create encoder for sending String messages to Topic
implicit val encoder: MessageEncoder[String] = (a: String) => Right(a.getBytes(java.nio.charset.Charset.defaultCharset()))

// Publish message to topic
val publishMsg = PSPublisher.produce[String]("String Message")

// Provide Publisher layer
publishMsg.provide(PSPublisher.live("gcsProjectId", "topic"))
```
### Subscriber
```scala mdoc:silent
import gcp4zio.pubsub.subscriber._
import zio._

// Create stream to consume messages from the subscription
val subscriberStream = PSSubscriber.subscribe("gcsProjectId", "subscription")

// Print first 10 messages from stream to console
val task = subscriberStream.mapZIO { msg =>
      ZIO.logInfo(msg.value.toString) *> msg.ack
    }
    .take(10)
    .runDrain
```
Check [this](examples/src/main/scala/PS.scala) example to use PubSub APIs   
  
## Monitoring API
```scala mdoc:silent
import gcp4zio.monitoring._
import com.google.monitoring.v3.TimeInterval

// Get GCS Cloud Monitoring metric data (time-series data)
Monitoring.getMetric(
  project = "gcsProjectId", 
  metric = "compute.googleapis.com/instance/cpu/usage_time", 
  interval = TimeInterval.getDefaultInstance  // Provide TimeInterval with start and end time
)
```
