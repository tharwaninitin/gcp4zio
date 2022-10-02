import zio.test._
//import gcp4zio.dp.DPLive
//import gcp4zio.dp.DPJobLive

object RunTests extends ZIOSpecDefault {
  // private val env = DPJobLive(dpEndpoint) ++ DPLive(dpEndpoint)

  override def spec: Spec[TestEnvironment, Any] = suite("DP Apis")(
    // DPCreateTestSuite.spec
    // DPStepsTestSuite.spec,
    // DPDeleteTestSuite.spec
  ) @@ TestAspect.sequential // .provideCustomLayerShared(env.orDie)
}
