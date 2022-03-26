# Gcp4zio
[![License](http://img.shields.io/:license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Tests](https://github.com/tharwaninitin/gcp4zio/actions/workflows/ci.yml/badge.svg)](https://github.com/tharwaninitin/gcp4zio/actions/workflows/ci.yml)
[![Semantic Versioning Policy Check](https://github.com/tharwaninitin/gcp4zio/actions/workflows/semver.yml/badge.svg)](https://github.com/tharwaninitin/gcp4zio/actions/workflows/semver.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.tharwaninitin/gcp4zio_2.12/badge.svg)](https://mvnrepository.com/artifact/com.github.tharwaninitin/gcp4zio)
[![javadoc](https://javadoc.io/badge2/com.github.tharwaninitin/gcp4zio_2.12/javadoc.svg)](https://javadoc.io/doc/com.github.tharwaninitin/gcp4zio_2.12)
[![gcp4zio Scala version support](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio/latest-by-scala-version.svg)](https://index.scala-lang.org/tharwaninitin/gcp4zio/gcp4zio)

**Gcp4zio** is simple Scala interface to Google Cloud API based on ZIO.

Add the latest release as a dependency to your project

[![Latest Version](https://maven-badges.herokuapp.com/maven-central/com.github.tharwaninitin/gcp4zio_2.12/badge.svg)](https://mvnrepository.com/artifact/com.github.tharwaninitin/gcp4zio)

__SBT__
```
libraryDependencies += "com.github.tharwaninitin" %% "gcp4zio" % "x.x.x"
```
__Maven__
```
<dependency>
    <groupId>com.github.tharwaninitin</groupId>
    <artifactId>gcp4zio_2.12</artifactId>
    <version>x.x.x</version>
</dependency>
```

***Google Cloud Storage API***
```scala
import gcp4zio.GCSApi

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
