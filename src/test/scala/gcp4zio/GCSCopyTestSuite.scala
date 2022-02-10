package gcp4zio

import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

object GCSCopyTestSuite extends TestHelper {
  val spec: ZSpec[environment.TestEnvironment with GCSEnv, Any] =
    suite("GCS Copy Apis")(
      testM("Execute copyObjectsGCStoGCS") {
        val step = GCSApi.copyObjectsGCStoGCS(gcs_bucket, "temp/test/", gcs_bucket, "temp2", 2, true)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute copyObjectsLOCALtoGCS") {
        val step = GCSApi.copyObjectsLOCALtoGCS("/local/path", gcs_bucket, "temp", 2, true)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      }
    ) @@ TestAspect.sequential
}
