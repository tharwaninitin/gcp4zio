import gcp4zio.bq._
import zio.test._

object RunTests extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment, Any] = suite("BQ Apis")(
    BQSchemaMappingTestSuite.spec
//    BQLoadExportTestSuite.spec,
//    BQQueryTestSuite.spec
  ) @@ TestAspect.sequential // .provideShared(BQ.live().orDie)
}
