import gcp4zio.bq.BQSchemaMappingTestSuite
import zio.test._
//import gcp4zio.bq.BQ

object RunTests extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment, Any] = suite("BQ Apis")(
    BQSchemaMappingTestSuite.spec
    // BQStepsTestSuite.spec,
  ) @@ TestAspect.sequential // .provideShared(BQ.live().orDie)
}
