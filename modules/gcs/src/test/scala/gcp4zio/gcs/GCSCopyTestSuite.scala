package gcp4zio.gcs

import gcp4zio.Global.{filePathCsv, gcsBucket}
import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test.{assertZIO, suite, test, Spec, TestAspect}

object GCSCopyTestSuite {
  val spec: Spec[GCS, Any] =
    suite("GCS Copy Apis")(
      test("Execute copyObjectsLOCALtoGCS single file") {
        val step = GCS.copyObjectsLOCALtoGCS(filePathCsv, gcsBucket, "temp/test/ratings.csv", 2, true)
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Execute copyObjectsLOCALtoGCS directory") {
        val step = GCS.copyObjectsLOCALtoGCS(filePathCsv.replaceAll("ratings.csv", ""), gcsBucket, "temp/test", 2, true)
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Execute copyObjectsGCStoGCS single file") {
        val step =
          GCS.copyObjectsGCStoGCS(
            srcBucket = gcsBucket,
            srcPrefix = Some("temp/test/ratings.csv"),
            targetBucket = gcsBucket,
            targetPrefix = Some("temp2/test/ratings.csv")
          )
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Execute copyObjectsGCStoGCS directory") {
        val step =
          GCS.copyObjectsGCStoGCS(
            gcsBucket,
            Some("temp/test/"),
            targetBucket = gcsBucket,
            targetPrefix = Some("temp2/test/")
          )
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      }
    ) @@ TestAspect.sequential
}
