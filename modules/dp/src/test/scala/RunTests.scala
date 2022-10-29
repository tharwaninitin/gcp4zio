import gcp4zio.Global._
import gcp4zio.dp._
import zio.test._

object RunTests extends ZIOSpecDefault {
  private val env = DPJob.live(dpEndpoint) ++ DPCluster.live(dpEndpoint)

  override def spec: Spec[TestEnvironment, Any] = (suite("DP Apis")(
    DPCreateTestSuite.spec,
    DPStepsTestSuite.spec,
    DPDeleteTestSuite.spec
  ) @@ TestAspect.sequential).provideShared(env.orDie)
}
