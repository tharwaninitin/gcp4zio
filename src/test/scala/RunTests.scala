import zio.test._
import gcp4zio._
//import zio._

object RunTests extends DefaultRunnableSpec {
  val dpEndpoint: String = sys.env.getOrElse("DP_ENDPOINT", "")

  // val env: Layer[Throwable, BQEnv with GCSEnv with DPEnv with DPJobEnv] = BQ.live() ++ GCS.live() ++ DP.live(dpEndpoint) ++ DPJob.live(dpEndpoint)

  override def spec: ZSpec[environment.TestEnvironment, Any] = suite("GCP Apis")(
    BQSchemaMappingTestSuite.spec @@ TestAspect.scala2Only
//    BQStepsTestSuite.spec,
//    GCSTestSuite.spec,
//    DPCreateTestSuite.spec,
//    DPStepsTestSuite.spec,
//    DPDeleteTestSuite.spec
  ) // .provideCustomLayerShared(env.orDie)
}
