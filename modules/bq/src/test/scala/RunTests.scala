import gcp4zio._
import zio.test._
//import gcp4zio.bq.BQLive

object RunTests extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment, Any] = suite("BQ Apis")(
    BQSchemaMappingTestSuite.spec
    // BQStepsTestSuite.spec,
  ) @@ TestAspect.sequential // .provideCustomLayerShared(BQLive().orDie)
}
