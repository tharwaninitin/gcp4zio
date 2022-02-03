package gcp4zio

import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

object GCSTestSuite extends TestHelper {
  val spec: ZSpec[environment.TestEnvironment with GCSEnv, Any] =
    suite("GCS Steps")(
      testM("Execute GCSPut PARQUET step") {
        val step = GCSApi.putObject(gcs_bucket, "temp/ratings.parquet", file_path_parquet)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute GCSPut CSV step") {
        val step = GCSApi.putObject(gcs_bucket, "temp/ratings.csv", file_path_csv)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute GCSCopy step LOCAL to GCS") {
        val step = GCSApi.copyObjectsLOCALtoGCS("/local/path", gcs_bucket, "temp", 2, true)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute GCSCopy step GCS to GCS") {
        val step = GCSApi.copyObjectsGCStoGCS(gcs_bucket, "temp", gcs_bucket, "temp2", 2, true)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      }
    ) @@ TestAspect.sequential
}
