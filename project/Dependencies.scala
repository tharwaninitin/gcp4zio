import Versions._
import sbt._

object Dependencies {

  lazy val core = List(
    "dev.zio"  %% "zio"       % ZioVersion,
    "org.slf4j" % "slf4j-api" % Sl4jVersion
  )

  lazy val testLibs = List(
    "dev.zio"       %% "zio-test"        % ZioVersion,
    "dev.zio"       %% "zio-test-sbt"    % ZioVersion,
    "ch.qos.logback" % "logback-classic" % LogbackVersion
  ).map(_ % Test)

}
