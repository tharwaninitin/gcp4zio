package gcp4zio

import gcp4zio.Global._
import gcp4zio.gcs.{GCSApi, GCSEnv}
import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

object GCSCopyTestSuite {
  val spec: Spec[GCSEnv, Any] =
    suite("GCS Copy Apis")(
      test("Execute copyObjectsLOCALtoGCS single file") {
        val step = GCSApi.copyObjectsLOCALtoGCS(filePathCsv, gcsBucket, "temp/test/ratings.csv", 2, true)
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Execute copyObjectsLOCALtoGCS directory") {
        val step = GCSApi.copyObjectsLOCALtoGCS(filePathCsv.replaceAll("ratings.csv", ""), gcsBucket, "temp/test", 2, true)
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Execute copyObjectsGCStoGCS single file") {
        val step =
          GCSApi.copyObjectsGCStoGCS(
            srcBucket = gcsBucket,
            srcPrefix = Some("temp/test/ratings.csv"),
            targetBucket = gcsBucket,
            targetPrefix = Some("temp2/test/ratings.csv")
          )
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Execute copyObjectsGCStoGCS directory") {
        val step =
          GCSApi.copyObjectsGCStoGCS(
            gcsBucket,
            Some("temp/test/"),
            targetBucket = gcsBucket,
            targetPrefix = Some("temp2/test/")
          )
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      }
    ) @@ TestAspect.sequential
}
