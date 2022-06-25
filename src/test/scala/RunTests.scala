import zio.test._
import gcp4zio._
//import gcp4zio.bq.BQLive
//import gcp4zio.dp.{DPJobLive, DPLive}
//import gcp4zio.gcs.GCSLive

object RunTests extends ZIOSpecDefault with TestHelper {

  // private val env = DPJobLive(dpEndpoint) ++ DPLive(dpEndpoint) ++ BQLive() ++ GCSLive()

  override def spec: Spec[TestEnvironment, Any] = suite("GCP Apis")(
    BQSchemaMappingTestSuite.spec
    // BQStepsTestSuite.spec,
    // GCSTestSuite.spec,
    // GCSCopyTestSuite.spec,
    // DPCreateTestSuite.spec
    // DPStepsTestSuite.spec,
    // DPDeleteTestSuite.spec
  ) @@ TestAspect.sequential // .provideCustomLayerShared(env.orDie)
}
