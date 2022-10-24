import ScalaCompileOptions._
import Versions._

lazy val examples = (project in file("examples"))
  .settings(
    name               := "examples",
    scalaVersion       := Scala212,
    publish / skip     := true,
    crossScalaVersions := AllScalaVersions,
    libraryDependencies ++= List(
      "com.github.tharwaninitin" %% "gcp4zio-dp"      % version.value,
      "com.github.tharwaninitin" %% "gcp4zio-gcs"     % version.value,
      "com.github.tharwaninitin" %% "gcp4zio-pubsub"  % version.value,
      "ch.qos.logback"            % "logback-classic" % LogbackVersion
    ),
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 12)) => s2copts ++ s212copts
        case Some((2, 13)) => s2copts
        case Some((3, _))  => s3copts
        case _             => Seq()
      }
    }
  )
