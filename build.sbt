import Dependencies._
import ScalaCompileOptions._
import Versions._

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val gcp4zio = (project in file("."))
  .settings(
    name         := "gcp4zio",
    version      := Gcp4ZioVersion,
    scalaVersion := scala212,
    libraryDependencies ++= javaLibs ++ scalaLibs ++ testLibs,
    crossScalaVersions         := allScalaVersions,
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
