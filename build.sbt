import Dependencies._
import ScalaCompileOptions._
import Versions._

lazy val gcp4s = (project in file("."))
  .settings(
    name         := "gcp4s",
    version      := "0.1.0",
    scalaVersion := scala212,
    libraryDependencies ++= core ++ testLibs,
    crossScalaVersions := allScalaVersions,
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
