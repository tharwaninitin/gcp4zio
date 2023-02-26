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
  Compile / compile / wartremoverErrors ++= Warts.allBut(
    Wart.Any,
    Wart.DefaultArguments,
    Wart.Nothing,
    Wart.Equals,
    Wart.FinalCaseClass,
    Wart.Overloading,
    Wart.StringPlusAny
  ),
  Test / parallelExecution := false,
  testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
)

lazy val gcp4zio = (project in file("."))
  .settings(
    crossScalaVersions := Nil, // crossScalaVersions must be set to Nil on the aggregating project
    publish / skip     := true
  )
  .aggregate(bq, dp, gcs, pubsub, batch, monitoring)

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

lazy val batch = (project in file("modules/batch"))
  .settings(commonSettings)
  .settings(name := "gcp4zio-batch", libraryDependencies ++= coreLibs ++ batchLibs ++ testLibs)

lazy val monitoring = (project in file("modules/monitoring"))
  .settings(commonSettings)
  .settings(name := "gcp4zio-monitoring", libraryDependencies ++= coreLibs ++ monitoringLibs ++ testLibs)

lazy val docs = project
  .in(file("modules/docs")) // important: it must not be docs/
  .dependsOn(bq, dp, gcs, pubsub, batch, monitoring)
  .settings(
    name           := "gcp4zio-docs",
    publish / skip := true,
    mdocVariables  := Map("VERSION" -> version.value, "Scala212" -> Scala212, "Scala213" -> Scala213, "Scala3" -> Scala3),
    mdocIn         := new File("docs/readme.template.md"),
    mdocOut        := new File("README.md")
  )
  .enablePlugins(MdocPlugin)
