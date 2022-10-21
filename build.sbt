import Dependencies._
import ScalaCompileOptions._
import Versions._

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val commonSettings = Seq(
  scalaVersion               := Scala212,
  crossScalaVersions         := AllScalaVersions,
  dependencyUpdatesFailBuild := true,
  dependencyUpdatesFilter -= moduleFilter(organization = "org.scala-lang"),
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 12)) => s2copts ++ s212copts
      case Some((2, 13)) => s2copts
      case Some((3, _))  => s3copts
      case _             => Seq()
    }
  },
  Test / parallelExecution := false,
  testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
)

lazy val gcp4zio = (project in file("."))
  .settings(
    crossScalaVersions := Nil, // crossScalaVersions must be set to Nil on the aggregating project
    publish / skip     := true
  )
  .aggregate(bq, dp, gcs)

lazy val bq = (project in file("modules/bq"))
  .settings(commonSettings)
  .settings(name := "gcp4zio-bq", libraryDependencies ++= coreLibs ++ bqLibs ++ testLibs)

lazy val dp = (project in file("modules/dp"))
  .settings(commonSettings)
  .settings(name := "gcp4zio-dp", libraryDependencies ++= coreLibs ++ dpLibs ++ testLibs)

lazy val gcs = (project in file("modules/gcs"))
  .settings(commonSettings)
  .settings(name := "gcp4zio-gcs", libraryDependencies ++= coreLibs ++ gcsLibs ++ testLibs)

lazy val pubsub = (project in file("modules/pubsub"))
  .settings(commonSettings)
  .settings(name := "gcp4zio-pubsub", libraryDependencies ++= coreLibs ++ pubSubLibs ++ testLibs)
