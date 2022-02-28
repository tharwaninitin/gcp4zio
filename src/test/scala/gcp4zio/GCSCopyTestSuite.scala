package gcp4zio

import zio._
import zio.test.Assertion.equalTo
import zio.test._

object GCSCopyTestSuite extends TestHelper {
  val spec: ZSpec[environment.TestEnvironment with GCSEnv, Any] =
    suite("GCS Copy Apis")(
      testM("Execute copyObjectsLOCALtoGCS single file") {
        val step = GCSApi.copyObjectsLOCALtoGCS(file_path_csv, gcs_bucket, "temp/test/ratings.csv", 2, true)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute copyObjectsLOCALtoGCS directory") {
        val step = GCSApi.copyObjectsLOCALtoGCS(file_path_csv.replaceAll("ratings.csv", ""), gcs_bucket, "temp/test", 2, true)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute copyObjectsGCStoGCS single file") {
        val step =
          GCSApi.copyObjectsGCStoGCS(
            src_bucket = gcs_bucket,
            src_prefix = Some("temp/test/ratings.csv"),
            target_bucket = gcs_bucket,
            target_prefix = Some("temp2/test/ratings.csv")
          )
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute copyObjectsGCStoGCS directory") {
        val step =
          GCSApi.copyObjectsGCStoGCS(
            gcs_bucket,
            Some("temp/test/"),
            target_bucket = gcs_bucket,
            target_prefix = Some("temp2/test/")
          )
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      }
    ) @@ TestAspect.sequential
}
