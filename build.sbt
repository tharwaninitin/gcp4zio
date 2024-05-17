import Dependencies._
import ScalaCompileOptions._
import Versions._

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val commonSettings = Seq(
  scalaVersion               := scala213,
  crossScalaVersions         := allScalaVersions,
  dependencyUpdatesFailBuild := true,
  compileOrder               := CompileOrder.JavaThenScala,
//  scalacOptions ++= Seq("-target:11"),
//  scalacOptions ++= Seq("-release", "25"),
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
  .aggregate(bq, dp, gcs, pubsub, batch, monitoring, examples)

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

lazy val examples = (project in file("examples"))
  .settings(commonSettings)
  .settings(
    name           := "examples",
    publish / skip := true,
    libraryDependencies ++= List("ch.qos.logback" % "logback-classic" % logbackVersion)
  )
  .dependsOn(dp, gcs, pubsub, batch)

lazy val docs = project
  .in(file("modules/docs")) // important: it must not be docs/
  .dependsOn(bq, dp, gcs, pubsub, batch, monitoring)
  .settings(
    name           := "gcp4zio-docs",
    publish / skip := true,
    mdocVariables  := Map("VERSION" -> version.value, "Scala212" -> scala212, "Scala213" -> scala213, "Scala3" -> scala3, "JavaVersions" -> "17, 21"),
    mdocIn         := new File("docs/readme.template.md"),
    mdocOut        := new File("README.md")
  )
  .enablePlugins(MdocPlugin)
