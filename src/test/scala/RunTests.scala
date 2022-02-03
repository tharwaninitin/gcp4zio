import zio.test._
import gcp4zio._
import zio._

object RunTests extends DefaultRunnableSpec {
  val dpEndpoint: String = sys.env.getOrElse("DP_ENDPOINT", "")

  type AllEnv = BQEnv with GCSEnv with DPEnv with DPJobEnv

  val env: TaskLayer[AllEnv] = BQ.live() ++ GCS.live() ++ DP.live(dpEndpoint) ++ DPJob.live(dpEndpoint)

  override def spec: ZSpec[environment.TestEnvironment, Any] = suite("GCP Apis")(
    BQSchemaMappingTestSuite.spec
//    BQStepsTestSuite.spec,
//    GCSTestSuite.spec,
//    DPCreateTestSuite.spec,
//    DPStepsTestSuite.spec,
//    DPDeleteTestSuite.spec
  ) // .provideCustomLayerShared(env.orDie)
}
