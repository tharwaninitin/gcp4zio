package gcp4zio

import com.google.cloud.storage.Storage.BlobTargetOption
import zio.stream.ZTransducer
import zio.test.Assertion.equalTo
import zio.test._
import zio.{UIO, ZIO}
import java.nio.file.Paths
import java.util.UUID

object GCSTestSuite extends TestHelper {
  val prefix = "temp/ratings.csv"
  val spec: ZSpec[environment.TestEnvironment with GCSEnv, Any] =
    suite("GCS Apis")(
      testM("Execute putObject") {
        val path = Paths.get(file_path_csv)
        val step = GCSApi.putObject(gcs_bucket, prefix, path, List.empty)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute lookupObject with not existing object") {
        val step = GCSApi.lookupObject(gcs_bucket, UUID.randomUUID().toString)
        assertM(step.foldM(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op.toString)))(equalTo("false"))
      },
      testM("Execute lookupObject with existing object") {
        val step = GCSApi.lookupObject(gcs_bucket, prefix)
        assertM(step.foldM(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op.toString)))(equalTo("true"))
      },
      testM("Execute putObject Overwrite Error") {
        val path  = Paths.get(file_path_csv)
        val opts  = List(BlobTargetOption.doesNotExist())
        val step  = GCSApi.putObject(gcs_bucket, prefix, path, opts)
        val error = "At least one of the pre-conditions you specified did not hold."
        assertM(step.foldM(ex => ZIO.succeed(ex.getMessage), _ => ZIO.fail("ok")))(equalTo(error))
      },
      testM("Execute getObject Streaming CSV") {
        val step = GCSApi
          .getObject(gcs_bucket, prefix)
          .transduce(ZTransducer.utf8Decode >>> ZTransducer.splitLines)
          .tap(op => UIO(logger.info(op)))
          .runCollect
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute getObject Streaming CSV putObject Streaming") {
        val sink = GCSApi.putObject(gcs_bucket, "temp/ratings1.csv", List.empty)
        val step = GCSApi.getObject(gcs_bucket, prefix).run(sink)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), op => ZIO.succeed(op.toString)))(equalTo("124"))
      },
      testM("Execute copyObjectsGCStoGCS") {
        val step = GCSApi.copyObjectsGCStoGCS(gcs_bucket, "temp", gcs_bucket, "temp2", 2, true)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute copyObjectsLOCALtoGCS") {
        val step = GCSApi.copyObjectsLOCALtoGCS("/local/path", gcs_bucket, "temp", 2, true)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      }
    ) @@ TestAspect.sequential
}
