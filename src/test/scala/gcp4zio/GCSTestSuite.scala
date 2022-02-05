package gcp4zio

import zio.stream.ZTransducer
import zio.{UIO, ZIO}
import zio.test.Assertion.equalTo
import zio.test._

object GCSTestSuite extends TestHelper {
  val spec: ZSpec[environment.TestEnvironment with GCSEnv, Any] =
    suite("GCS Apis")(
      testM("Execute GCSPut") {
        val step = GCSApi.putObject(gcs_bucket, "temp/ratings.csv", file_path_csv)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute GCSGet CSV") {
        val step = GCSApi
          .getObject(gcs_bucket, "temp/ratings.csv")
          .transduce(ZTransducer.utf8Decode >>> ZTransducer.splitLines)
          .tap(op => UIO(println(op)))
          .runCollect
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute GCSCopy LOCAL to GCS") {
        val step = GCSApi.copyObjectsLOCALtoGCS("/local/path", gcs_bucket, "temp", 2, true)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute GCSCopy GCS to GCS") {
        val step = GCSApi.copyObjectsGCStoGCS(gcs_bucket, "temp", gcs_bucket, "temp2", 2, true)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      }
    ) @@ TestAspect.sequential
}
