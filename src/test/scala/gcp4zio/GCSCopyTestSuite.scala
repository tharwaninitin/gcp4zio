package gcp4zio

import zio._
import zio.test.Assertion.equalTo
import zio.test._

object GCSCopyTestSuite extends TestHelper {
  val spec: ZSpec[environment.TestEnvironment with GCSEnv, Any] =
    suite("GCS Copy Apis")(
      testM("Execute copyObjectsLOCALtoGCS single file") {
        val step = GCSApi.copyObjectsLOCALtoGCS(filePathCsv, gcsBucket, "temp/test/ratings.csv", 2, true)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute copyObjectsLOCALtoGCS directory") {
        val step = GCSApi.copyObjectsLOCALtoGCS(filePathCsv.replaceAll("ratings.csv", ""), gcsBucket, "temp/test", 2, true)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute copyObjectsGCStoGCS single file") {
        val step =
          GCSApi.copyObjectsGCStoGCS(
            srcBucket = gcsBucket,
            srcPrefix = Some("temp/test/ratings.csv"),
            targetBucket = gcsBucket,
            targetPrefix = Some("temp2/test/ratings.csv")
          )
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute copyObjectsGCStoGCS directory") {
        val step =
          GCSApi.copyObjectsGCStoGCS(
            gcsBucket,
            Some("temp/test/"),
            targetBucket = gcsBucket,
            targetPrefix = Some("temp2/test/")
          )
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      }
    ) @@ TestAspect.sequential
}
