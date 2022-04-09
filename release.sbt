import sbtrelease._
import sbtrelease.ReleaseStateTransformations._

releaseVersion := setReleaseVersionFunction(versionPolicyIntention.value)

def setReleaseVersionFunction(compatibilityIntention: Compatibility): String => String = {
  val maybeBump = compatibilityIntention match {
    case Compatibility.None                      => Some(Version.Bump.Minor) // For "early-semver" - major version is 0
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

/* Note that we are skipping some steps as we are using the maven plugin.
 * Should remove that later when we get publishArtifacts working
 */
releaseIgnoreUntrackedFiles := true
releaseCrossBuild           := false
releaseNextVersion := { ver =>
  Version(ver).map(_.bump(releaseVersionBump.value).string).getOrElse(versionFormatError(ver))
}
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  setReleaseVersion,
  releaseStepCommand("versionPolicyCheck"),       // Run task `versionPolicyCheck` after the release version is set
  releaseStepTask(setNextCompatibilityIntention), // Reset compatibility intention to `Compatibility.BinaryAndSourceCompatible`
  releaseStepCommandAndRemaining("+publish"),
  setNextVersion
)
