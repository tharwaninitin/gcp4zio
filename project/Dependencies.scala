import Versions._
import sbt._

object Dependencies {

  lazy val core = List(
    "dev.zio"                %% "zio"                     % ZioVersion,
    "org.scala-lang.modules" %% "scala-collection-compat" % ScalaJavaCollectionCompat,
    "com.google.cloud"        % "google-cloud-bigquery"   % GcpBqVersion,
    "com.google.cloud"        % "google-cloud-dataproc"   % GcpDpVersion,
    "com.google.cloud"        % "google-cloud-storage"    % GcpGcsVersion,
    "org.slf4j"               % "slf4j-api"               % Sl4jVersion
  )

  lazy val testLibs = List(
    "dev.zio"       %% "zio-test"        % ZioVersion,
    "dev.zio"       %% "zio-test-sbt"    % ZioVersion,
    "ch.qos.logback" % "logback-classic" % LogbackVersion
  ).map(_ % Test)

}
