# Gcp4zio
[![License](http://img.shields.io/:license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Tests](https://github.com/tharwaninitin/gcp4zio/actions/workflows/ci.yml/badge.svg)](https://github.com/tharwaninitin/gcp4zio/actions/workflows/ci.yml)
[![Semantic Versioning Policy Check](https://github.com/tharwaninitin/gcp4zio/actions/workflows/semver.yml/badge.svg)](https://github.com/tharwaninitin/gcp4zio/actions/workflows/semver.yml)

**Gcp4zio** is simple Scala interface to Google Cloud API based on ZIO.

Add the latest release as a dependency to your project

| Module               | Latest Version                                                                                                                                                                                                   |                                                                                                                                                                 Documentation | Scala Versions                                                                                                                                                                                                          | 
|----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Google Cloud Storage | [![Latest Version](https://maven-badges.herokuapp.com/maven-central/com.github.tharwaninitin/gcp4zio-gcs_2.12/badge.svg)](https://mvnrepository.com/artifact/com.github.tharwaninitin/gcp4zio-gcs)               |               [![javadoc](https://javadoc.io/badge2/com.github.tharwaninitin/gcp4zio-gcs_2.12/javadoc.svg)](https://javadoc.io/doc/com.github.tharwaninitin/gcp4zio-gcs_2.12) | [![gcp4zio-gcs Scala version support](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-gcs/latest-by-scala-version.svg)](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-gcs)                      |
| Dataproc             | [![Latest Version](https://maven-badges.herokuapp.com/maven-central/com.github.tharwaninitin/gcp4zio-dp_2.12/badge.svg)](https://mvnrepository.com/artifact/com.github.tharwaninitin/gcp4zio-dp)                 |                 [![javadoc](https://javadoc.io/badge2/com.github.tharwaninitin/gcp4zio-dp_2.12/javadoc.svg)](https://javadoc.io/doc/com.github.tharwaninitin/gcp4zio-dp_2.12) | [![gcp4zio-dp Scala version support](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-dp/latest-by-scala-version.svg)](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-dp)                         |
| BigQuery             | [![Latest Version](https://maven-badges.herokuapp.com/maven-central/com.github.tharwaninitin/gcp4zio-bq_2.12/badge.svg)](https://mvnrepository.com/artifact/com.github.tharwaninitin/gcp4zio-bq)                 |                 [![javadoc](https://javadoc.io/badge2/com.github.tharwaninitin/gcp4zio-bq_2.12/javadoc.svg)](https://javadoc.io/doc/com.github.tharwaninitin/gcp4zio-bq_2.12) | [![gcp4zio-bq Scala version support](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-bq/latest-by-scala-version.svg)](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-bq)                         |
| PubSub               | [![Latest Version](https://maven-badges.herokuapp.com/maven-central/com.github.tharwaninitin/gcp4zio-pubsub_2.12/badge.svg)](https://mvnrepository.com/artifact/com.github.tharwaninitin/gcp4zio-pubsub)         |         [![javadoc](https://javadoc.io/badge2/com.github.tharwaninitin/gcp4zio-pubsub_2.12/javadoc.svg)](https://javadoc.io/doc/com.github.tharwaninitin/gcp4zio-pubsub_2.12) | [![gcp4zio-pubsub Scala version support](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-pubsub/latest-by-scala-version.svg)](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-pubsub)             |
| Cloud Monitoring     | [![Latest Version](https://maven-badges.herokuapp.com/maven-central/com.github.tharwaninitin/gcp4zio-monitoring_2.12/badge.svg)](https://mvnrepository.com/artifact/com.github.tharwaninitin/gcp4zio-monitoring) | [![javadoc](https://javadoc.io/badge2/com.github.tharwaninitin/gcp4zio-monitoring_2.12/javadoc.svg)](https://javadoc.io/doc/com.github.tharwaninitin/gcp4zio-monitoring_2.12) | [![gcp4zio-monitoring Scala version support](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-monitoring/latest-by-scala-version.svg)](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-monitoring) |

__SBT__
``` scala mdoc
libraryDependencies ++= List(
      "com.github.tharwaninitin" %% "gcp4zio-gcs" % @VERSION@,
      "com.github.tharwaninitin" %% "gcp4zio-dp"  % @VERSION@,
      "com.github.tharwaninitin" %% "gcp4zio-bq"  % @VERSION@,
      "com.github.tharwaninitin" %% "gcp4zio-pubsub"  % @VERSION@,
      "com.github.tharwaninitin" %% "gcp4zio-monitoring"  % @VERSION@
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
<!-- TOC -->
- [GCP4ZIO API's](#gcp4zio-apis)
  - [Google Cloud Storage](#google-cloud-storage-api)
  - [Dataproc](#dataproc-api)
    - [Dataproc Cluster](#dataproc-cluster-api)
    - [Dataproc Job](#dataproc-job-api)
  - [Bigquery](#bigquery-api)
  - [PubSub](#pubsub-api)
    - [Topic](#topic-api)
    - [Subscription](#subscription-api)
    - [Publisher](#publisher-api)
    - [Subscriber](#subscriber-api)
  - [Monitoring](#monitoring-api)
<!-- /TOC -->

## Google Cloud Storage API
```scala mdoc:silent
import gcp4zio.gcs._

// Copy single object from source bucket to target bucket
GCSApi.copyObjectsGCStoGCS(
  srcBucket = "src_gcs_bucket",
  srcPrefix = Some("temp/test/ratings.csv"),
  targetBucket = "tgt_gcs_bucket",
  targetPrefix = Some("temp2/test/ratings.csv")
)

// Copy all objects from source bucket to target bucket
GCSApi.copyObjectsGCStoGCS(
  srcBucket = "src_gcs_bucket",
  targetBucket = "tgt_gcs_bucket"
)

// Copy all objects from source bucket with prefix to target bucket
GCSApi.copyObjectsGCStoGCS(
  srcBucket = "src_gcs_bucket",
  srcPrefix = Some("temp/test"),
  targetBucket = "tgt_gcs_bucket"
)
```  

## Dataproc API
### Dataproc Cluster API
```scala
//TODO
```  

### Dataproc Job API
```scala
//TODO
```  

## Bigquery API
```scala
//TODO
```  

## PubSub API
### Topic API
```scala mdoc:silent
import gcp4zio.pubsub.topic._

// Create PubSub Topic
PSTopic.createTopic(project = "PROJECT_ID", topic = "topicName")

// Delete PubSub Topic
PSTopic.deleteTopic(project = "PROJECT_ID", topic = "topicName")
```
### Subscription API
```scala mdoc:silent
import gcp4zio.pubsub.subscription._

// Create Pull Subscription
PSSubscription.createPullSubscription(
    project = "PROJECT_ID", 
    subscription = "subName", 
    topic = "topicName",
    ackDeadlineSeconds = 20 // default 10 seconds
  )

// Create Push Subscription
PSSubscription.createPushSubscription(
    project = "PROJECT_ID",
    subscription = "subName",
    topic = "topicName",
    ackDeadlineSeconds = 20, // default 10 seconds
    pushEndpoint = "https://example.com/push"
  )

// Create Bigquery Subscription
PSSubscription.createBQSubscription(
    project = "PROJECT_ID",
    subscription = "subName",
    topic = "topicName",
    bqTableId = "projectId:datasetId.tableId"
  )

// Delete Subscription
PSSubscription.deleteSubscription(
  project = "PROJECT_ID",
  subscription = "subName"
)
```
### Publisher API
```scala mdoc:silent
import gcp4zio.pubsub.publisher._

// Create encoder for sending String messages to Topic
implicit val encoder: MessageEncoder[String] = (a: String) => Right(a.getBytes(java.nio.charset.Charset.defaultCharset()))

// Publish message to topic
val publishMsg = PSPublisher.produce[String]("String Message")

// Provide Publisher layer
publishMsg.provide(PSPublisher.live("gcsProject", "topic"))
```
### Subscriber API
```scala mdoc:silent
import gcp4zio.pubsub.subscriber._
import zio._

// Create stream to consume messages from the subscription
val subscriberStream = PSSubscriber.subscribe

// Print first 10 messages from stream to console
val task = subscriberStream.mapZIO { msg =>
      ZIO.logInfo(msg.value.toString) *> msg.ack
    }
    .take(10)
    .runDrain

// Provide Publisher layer
task.provideSome[Scope](PSSubscriber.live("gcsProject", "subscription"))
```
Check [this](examples/src/main/scala/PS.scala) example to use PubSub APIs   
  
## Monitoring API
```scala
import gcp4zio.monitoring._

// Get GCS Cloud Monitoring metric data (time-series data)
MonitoringApi.getMetric(
  project = "PROJECT_ID", 
  metric = "compute.googleapis.com/instance/cpu/usage_time", 
  interval = TimeInterval.getDefaultInstance  // Provide TimeInterval with start and end time
)
```
