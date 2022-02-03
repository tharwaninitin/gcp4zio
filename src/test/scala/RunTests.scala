import zio.test._
import gcp4zio._

object RunTests extends DefaultRunnableSpec with TestHelper {

  // val env = DPJob.live(dp_endpoint) ++ DP.live(dp_endpoint) ++ BQ.live() ++ GCS.live()

  override def spec: ZSpec[environment.TestEnvironment, Any] = suite("GCP Apis")(
    BQSchemaMappingTestSuite.spec
    // BQStepsTestSuite.spec,
    // GCSTestSuite.spec,
    // DPCreateTestSuite.spec
    // DPStepsTestSuite.spec,
    // DPDeleteTestSuite.spec
  ) @@ TestAspect.sequential // .provideCustomLayerShared(env.orDie)
}
