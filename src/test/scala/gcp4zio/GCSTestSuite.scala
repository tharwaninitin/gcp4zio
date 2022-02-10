package gcp4zio

import com.google.cloud.storage.Storage.{BlobTargetOption, BlobWriteOption}
import zio.test.Assertion.equalTo
import zio.stream._
import zio.test._
import zio._
import java.nio.file.Paths
import java.util.UUID

object GCSTestSuite extends TestHelper {
  val prefix = "temp/test/ratings.csv"
  val spec: ZSpec[environment.TestEnvironment with GCSEnv, Any] =
    suite("GCS Apis")(
      testM("Execute putObject") {
        val path = Paths.get(file_path_csv)
        val step = GCSApi.putObject(gcs_bucket, prefix, path, List.empty)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute putObject with Overwrite Error") {
        val path  = Paths.get(file_path_csv)
        val opts  = List(BlobTargetOption.doesNotExist())
        val step  = GCSApi.putObject(gcs_bucket, prefix, path, opts)
        val error = "At least one of the pre-conditions you specified did not hold."
        assertM(step.foldM(ex => ZIO.succeed(ex.getMessage), _ => ZIO.fail("ok")))(equalTo(error))
      },
      testM("Execute lookupObject with existing object") {
        val step = GCSApi.lookupObject(gcs_bucket, prefix)
        assertM(step.foldM(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op.toString)))(equalTo("true"))
      },
      testM("Execute lookupObject with non existing object") {
        val step = GCSApi.lookupObject(gcs_bucket, UUID.randomUUID().toString)
        assertM(step.foldM(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op.toString)))(equalTo("false"))
      },
      testM("Execute(streaming) getObject") {
        val step = GCSApi
          .getObject(gcs_bucket, prefix, 4096)
          .transduce(ZTransducer.utf8Decode >>> ZTransducer.splitLines)
          .tap(op => UIO(logger.info(op)))
          .runDrain
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute(streaming) getObject => putObject") {
        val opts = List(BlobWriteOption.doesNotExist())
        val sink = GCSApi.putObject(gcs_bucket, "temp/test/ratings2.csv", opts)
        val step = GCSApi.getObject(gcs_bucket, prefix, 4096).run(sink)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), op => ZIO.succeed(op.toString)))(equalTo("124"))
      },
      testM("Execute delObject with existing object") {
        val effect = GCSApi.deleteObject(gcs_bucket, "temp/test/ratings2.csv")
        assertM(effect.foldM(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op.toString)))(equalTo("true"))
      },
      testM("Execute delObject with non existing object") {
        val effect = GCSApi.deleteObject(gcs_bucket, "temp/test/ratings2.csv")
        assertM(effect.foldM(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op.toString)))(equalTo("false"))
      },
      testM("Execute(streaming) listObjects => getObject") {
        val transducer: Transducer[Throwable, Byte, String] = ZTransducer.utf8Decode >>> ZTransducer.splitLines
        val logStream                                       = Stream.fromEffect(UIO(logger.info("#" * 50)))
        val stream = GCSApi
          .listObjects(gcs_bucket, "temp/test/", false, List.empty)
          .flatMap { blob =>
            logger.info(blob.getName)
            GCSApi.getObject(gcs_bucket, blob.getName, 4096).transduce(transducer).tap(line => UIO(logger.info(line)))
          }
        val effect = (logStream ++ stream ++ logStream).runDrain
        effect.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")).as(assertCompletes)
      }
    ) @@ TestAspect.sequential
}
