package gcp4zio

import com.google.cloud.storage.Storage.BlobTargetOption
import zio.stream.ZTransducer
import zio.test.Assertion.equalTo
import zio.test._
import zio.{UIO, ZIO}
import java.nio.file.Paths

object GCSTestSuite extends TestHelper {
  val spec: ZSpec[environment.TestEnvironment with GCSEnv, Any] =
    suite("GCS Apis")(
      testM("Execute GCSPut") {
        val step = GCSApi.putObject(gcs_bucket, "temp/ratings.csv", file_path_csv)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute GCSPut Overwrite Error") {
        val path  = Paths.get(file_path_csv)
        val opts  = List(BlobTargetOption.doesNotExist())
        val step  = GCSApi.putObject(gcs_bucket, "temp/ratings.csv", path, opts)
        val error = "At least one of the pre-conditions you specified did not hold."
        assertM(step.foldM(ex => ZIO.succeed(ex.getMessage), _ => ZIO.fail("ok")))(equalTo(error))
      },
      testM("Execute GCSGet Streaming CSV") {
        val step = GCSApi
          .getObject(gcs_bucket, "temp/ratings.csv")
          .transduce(ZTransducer.utf8Decode >>> ZTransducer.splitLines)
          .tap(op => UIO(logger.info(op)))
          .runCollect
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute GCSGet Streaming CSV GCSPut Streaming") {
        val sink = GCSApi.putObject(gcs_bucket, "temp/ratings1.csv", List.empty)
        val step = GCSApi.getObject(gcs_bucket, "temp/ratings.csv").run(sink)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), op => ZIO.succeed(op.toString)))(equalTo("124"))
      },
      testM("Execute GCSCopy GCS to GCS") {
        val step = GCSApi.copyObjectsGCStoGCS(gcs_bucket, "temp", gcs_bucket, "temp2", 2, true)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute GCSCopy LOCAL to GCS") {
        val step = GCSApi.copyObjectsLOCALtoGCS("/local/path", gcs_bucket, "temp", 2, true)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      }
    ) @@ TestAspect.sequential
}
