import Versions._
import sbt._

object Dependencies {

  lazy val scalaLibs = List(
    "dev.zio"                %% "zio"                     % ZioVersion,
    "dev.zio"                %% "zio-streams"             % ZioVersion,
    "org.scala-lang.modules" %% "scala-collection-compat" % ScalaJavaCollectionCompat
  )

  lazy val javaLibs = List(
    "com.google.cloud" % "google-cloud-bigquery" % GcpBqVersion,
    "com.google.cloud" % "google-cloud-dataproc" % GcpDpVersion,
    "com.google.cloud" % "google-cloud-storage"  % GcpGcsVersion,
    "org.slf4j"        % "slf4j-api"             % Sl4jVersion
  )

  lazy val testLibs = List(
    "dev.zio"       %% "zio-test"        % ZioVersion,
    "dev.zio"       %% "zio-test-sbt"    % ZioVersion,
    "ch.qos.logback" % "logback-classic" % LogbackVersion
  ).map(_ % Test)

}
