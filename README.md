# Gcp4zio
[![License](http://img.shields.io/:license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Tests](https://github.com/tharwaninitin/gcp4zio/actions/workflows/ci.yml/badge.svg)](https://github.com/tharwaninitin/gcp4zio/actions/workflows/ci.yml)
[![Semantic Versioning Policy Check](https://github.com/tharwaninitin/gcp4zio/actions/workflows/semver.yml/badge.svg)](https://github.com/tharwaninitin/gcp4zio/actions/workflows/semver.yml)

**Gcp4zio** is simple Scala interface to Google Cloud API based on ZIO.

Add the latest release as a dependency to your project

| Module Name | Latest Version                                                                                                                                                                                                   |                                                                                                                                                                 Documentation | Scala Versions                                                                                                                                                                                                      | 
|-------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| GCS         | [![Latest Version](https://maven-badges.herokuapp.com/maven-central/com.github.tharwaninitin/gcp4zio-gcs_2.12/badge.svg)](https://mvnrepository.com/artifact/com.github.tharwaninitin/gcp4zio-gcs)               |               [![javadoc](https://javadoc.io/badge2/com.github.tharwaninitin/gcp4zio-gcs_2.12/javadoc.svg)](https://javadoc.io/doc/com.github.tharwaninitin/gcp4zio-gcs_2.12) | [![gcp4zio-gcs Scala version support](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-gcs/latest-by-scala-version.svg)](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-gcs)                  |
| DP          | [![Latest Version](https://maven-badges.herokuapp.com/maven-central/com.github.tharwaninitin/gcp4zio-dp_2.12/badge.svg)](https://mvnrepository.com/artifact/com.github.tharwaninitin/gcp4zio-dp)                 |                 [![javadoc](https://javadoc.io/badge2/com.github.tharwaninitin/gcp4zio-dp_2.12/javadoc.svg)](https://javadoc.io/doc/com.github.tharwaninitin/gcp4zio-dp_2.12) | [![gcp4zio-dp Scala version support](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-dp/latest-by-scala-version.svg)](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-dp)                     |
| BQ          | [![Latest Version](https://maven-badges.herokuapp.com/maven-central/com.github.tharwaninitin/gcp4zio-bq_2.12/badge.svg)](https://mvnrepository.com/artifact/com.github.tharwaninitin/gcp4zio-bq)                 |                 [![javadoc](https://javadoc.io/badge2/com.github.tharwaninitin/gcp4zio-bq_2.12/javadoc.svg)](https://javadoc.io/doc/com.github.tharwaninitin/gcp4zio-bq_2.12) | [![gcp4zio-bq Scala version support](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-bq/latest-by-scala-version.svg)](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-bq)                     |
| PubSub      | [![Latest Version](https://maven-badges.herokuapp.com/maven-central/com.github.tharwaninitin/gcp4zio-pubsub_2.12/badge.svg)](https://mvnrepository.com/artifact/com.github.tharwaninitin/gcp4zio-pubsub)         |         [![javadoc](https://javadoc.io/badge2/com.github.tharwaninitin/gcp4zio-pubsub_2.12/javadoc.svg)](https://javadoc.io/doc/com.github.tharwaninitin/gcp4zio-pubsub_2.12) | [![gcp4zio-pubsub Scala version support](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-pubsub/latest-by-scala-version.svg)](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-pubsub)         |
| Monitoring  | [![Latest Version](https://maven-badges.herokuapp.com/maven-central/com.github.tharwaninitin/gcp4zio-monitoring_2.12/badge.svg)](https://mvnrepository.com/artifact/com.github.tharwaninitin/gcp4zio-monitoring) | [![javadoc](https://javadoc.io/badge2/com.github.tharwaninitin/gcp4zio-monitoring_2.12/javadoc.svg)](https://javadoc.io/doc/com.github.tharwaninitin/gcp4zio-monitoring_2.12) | [![gcp4zio-monitoring Scala version support](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-monitoring/latest-by-scala-version.svg)](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio-pubsub) |





__SBT__
```
libraryDependencies ++= List(
      "com.github.tharwaninitin" %% "gcp4zio-gcs" % x.x.x,
      "com.github.tharwaninitin" %% "gcp4zio-dp"  % x.x.x,
      "com.github.tharwaninitin" %% "gcp4zio-bq"  % x.x.x,
      "com.github.tharwaninitin" %% "gcp4zio-pubsub"  % x.x.x,
      "com.github.tharwaninitin" %% "gcp4zio-monitoring"  % x.x.x
   )
```
__Maven__
```
<dependency>
    <groupId>com.github.tharwaninitin</groupId>
    <artifactId>gcp4zio-gcs_2.12</artifactId>
    <version>x.x.x</version>
</dependency>
```

***Google Cloud Storage API***
```scala
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
***Dataproc API***
```scala
//TODO
```
***Dataproc Job API***
```scala
//TODO
```
***Bigquery API***
```scala
//TODO
```
***PubSub API***
```scala
//TODO
```
***PubSub API***
```scala
// Get GCS Cloud Monitoring metric data (time-series data)
MonitoringApi.getMetric(
  project = "PROJECT_ID", 
  metric = "compute.googleapis.com/instance/cpu/usage_time", 
  interval = TimeInterval.getDefaultInstance // Provide TimeInterval with start and end time
)
```
