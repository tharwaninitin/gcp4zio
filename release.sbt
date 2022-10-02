import sbtrelease._
import sbtrelease.ReleaseStateTransformations._

def setReleaseVersionFunction(compatibilityIntention: Compatibility): String => String = {
  val maybeBump = compatibilityIntention match {
    case Compatibility.None                      => Some(Version.Bump.Major)
    case Compatibility.BinaryCompatible          => Some(Version.Bump.Minor)
    case Compatibility.BinaryAndSourceCompatible => None
    // No need to bump the patch version, because it has already been bumped when sbt-release set the next release version
  }
  { (currentVersion: String) =>
    val versionWithoutQualifier =
      Version(currentVersion)
        .getOrElse(versionFormatError(currentVersion))
        .withoutQualifier
    (maybeBump match {
      case Some(bump) => versionWithoutQualifier.bump(bump)
      case None       => versionWithoutQualifier
    }).string
  }
}

lazy val setNextCompatibilityIntention = taskKey[Unit]("Set versionPolicyIntention to Compatibility.BinaryAndSourceCompatible")

ThisBuild / setNextCompatibilityIntention := {
  val log       = streams.value.log
  val intention = (ThisBuild / versionPolicyIntention).value
  if (intention == Compatibility.BinaryAndSourceCompatible) {
    log.info("Not changing compatibility intention because it is already set to BinaryAndSourceCompatible")
  } else {
    log.info("Reset compatibility intention to BinaryAndSourceCompatible")
    IO.write(
      new File("compatibility.sbt"),
      "ThisBuild / versionPolicyIntention := Compatibility.BinaryAndSourceCompatible\n"
    )
  }
}

releaseIgnoreUntrackedFiles := true
releaseCrossBuild           := false
releaseVersion              := setReleaseVersionFunction(versionPolicyIntention.value)
releaseNextVersion := { ver =>
  Version(ver).map(_.bump(releaseVersionBump.value).string).getOrElse(versionFormatError(ver))
}
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  setReleaseVersion,                              // Update current release version in version.sbt
  releaseStepCommand("versionPolicyCheck"),       // Run task `versionPolicyCheck` after the release version is set
  releaseStepTask(setNextCompatibilityIntention), // Reset compatibility intention to `Compatibility.BinaryAndSourceCompatible`
  releaseStepCommandAndRemaining("+publish"),
  setNextVersion // Update future release version in version.sbt
)
