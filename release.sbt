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

lazy val setAndCommitNextCompatibilityIntention =
  taskKey[Unit]("Set versionPolicyIntention to Compatibility.BinaryAndSourceCompatible, and commit the change")

ThisBuild / setAndCommitNextCompatibilityIntention := {
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
    val gitAddExitValue =
      sys.process.Process("git add compatibility.sbt").run(log).exitValue()
    assert(gitAddExitValue == 0, s"Command failed with exit status $gitAddExitValue")
    val gitCommitExitValue =
      sys.process
        .Process(Seq("git", "commit", "-m", "Reset compatibility intention"))
        .run(log)
        .exitValue()
    assert(gitCommitExitValue == 0, s"Command failed with exist status $gitCommitExitValue")
  }
}

/* Note that we are skipping some steps as we are using the maven plugin.
 * Should remove that later when we get publishArtifacts working
 */
releaseIgnoreUntrackedFiles := true
releaseCrossBuild           := false
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,                // : ReleaseStep
  inquireVersions,                          // : ReleaseStep
  setReleaseVersion,                        // : ReleaseStep
  releaseStepCommand("versionPolicyCheck"), // Run task `versionCheck` after the release version is set
  releaseStepTask(
    setAndCommitNextCompatibilityIntention
  ), // Reset compatibility intention to `Compatibility.BinaryAndSourceCompatible`
  // commitReleaseVersion,                            // : ReleaseStep, performs the initial git checks
  // tagRelease,     // : ReleaseStep
  setNextVersion, // : ReleaseStep
  // commitNextVersion,                               // : ReleaseStep
  releaseStepCommandAndRemaining("+publish") // : ReleaseStep, checks whether `publishTo` is properly set up
  // pushChanges // : ReleaseStep, also checks that an upstream branch is properly configured
)
