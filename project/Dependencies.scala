import Versions._
import sbt._

object Dependencies {

  lazy val coreLibs: List[ModuleID] = List(
    "dev.zio"                %% "zio"                     % zioVersion,
    "dev.zio"                %% "zio-streams"             % zioVersion,
    "org.scala-lang.modules" %% "scala-collection-compat" % scalaJavaCollectionCompat,
    "dev.zio"                %% "zio-logging-slf4j"       % zioLogVersion
  )

  lazy val bqLibs: List[ModuleID] = List(
    "com.google.cloud" % "google-cloud-bigquery" % gcpBqVersion
  )

  lazy val dpLibs: List[ModuleID] = List(
    "com.google.cloud" % "google-cloud-dataproc" % gcpDpVersion
  )

  lazy val gcsLibs: List[ModuleID] = List(
    "com.google.cloud" % "google-cloud-storage" % gcpGcsVersion
  )

  lazy val pubSubLibs: List[ModuleID] = List(
    "com.google.cloud" % "google-cloud-pubsub" % gcpPubSubVersion
  )

  lazy val monitoringLibs: List[ModuleID] = List(
    "com.google.cloud" % "google-cloud-monitoring" % cloudMonitorVersion
  )

  lazy val batchLibs: List[ModuleID] = List(
    "com.google.cloud" % "google-cloud-batch" % batchVersion
  )

  lazy val testLibs: List[ModuleID] = List(
    "dev.zio"       %% "zio-test"        % zioVersion,
    "dev.zio"       %% "zio-test-sbt"    % zioVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion
  ).map(_ % Test)

}
