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
        val path = Paths.get(filePathCsv)
        val step = GCSApi.putObject(gcsBucket, prefix, path, List.empty)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute putObject with Overwrite Error") {
        val path  = Paths.get(filePathCsv)
        val opts  = List(BlobTargetOption.doesNotExist())
        val step  = GCSApi.putObject(gcsBucket, prefix, path, opts)
        val error = "At least one of the pre-conditions you specified did not hold."
        assertM(step.foldM(ex => ZIO.succeed(ex.getMessage), _ => ZIO.fail("ok")))(equalTo(error))
      },
      testM("Execute lookupObject with existing object") {
        val step = GCSApi.lookupObject(gcsBucket, prefix)
        assertM(step.foldM(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op.toString)))(equalTo("true"))
      },
      testM("Execute lookupObject with non existing object") {
        val step = GCSApi.lookupObject(gcsBucket, UUID.randomUUID().toString)
        assertM(step.foldM(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op.toString)))(equalTo("false"))
      },
      testM("Execute(streaming) getObject") {
        val step = GCSApi
          .getObject(gcsBucket, prefix, 4096)
          .transduce(ZTransducer.utf8Decode >>> ZTransducer.splitLines)
          .tap(op => UIO(logger.info(op)))
          .runDrain
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute(streaming) getObject => putObject") {
        val opts = List(BlobWriteOption.doesNotExist())
        val sink = GCSApi.putObject(gcsBucket, "temp/test/ratings2.csv", opts)
        val step = GCSApi.getObject(gcsBucket, prefix, 4096).run(sink)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), op => ZIO.succeed(op.toString)))(equalTo("124"))
      },
      testM("Execute delObject with existing object") {
        val effect = GCSApi.deleteObject(gcsBucket, "temp/test/ratings2.csv")
        assertM(effect.foldM(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op.toString)))(equalTo("true"))
      },
      testM("Execute delObject with non existing object") {
        val effect = GCSApi.deleteObject(gcsBucket, "temp/test/ratings2.csv")
        assertM(effect.foldM(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op.toString)))(equalTo("false"))
      },
      testM("Execute listObjects to get size of all objects in bucket") {
        val mb = 1024 * 1024
        val effect =
          GCSApi.listObjects(gcsBucket).map(_.getSize).fold(0L)(_ + _).tap(s => UIO(logger.info(s"Total Size => ${s / mb} MB")))
        assertM(effect.foldM(ex => ZIO.fail(ex.toString), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute(streaming) listObjects => getObject") {
        val transducer: Transducer[Throwable, Byte, String] = ZTransducer.utf8Decode >>> ZTransducer.splitLines
        val logStream                                       = Stream.fromEffect(UIO(logger.info("#" * 50)))
        val stream = GCSApi
          .listObjects(gcsBucket, Some("temp/test/"), recursive = false, List.empty)
          .flatMap { blob =>
            logger.info(blob.getName)
            GCSApi.getObject(gcsBucket, blob.getName, 4096).transduce(transducer).tap(line => UIO(logger.info(line)))
          }
        val effect = (logStream ++ stream ++ logStream).runDrain
        effect.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")).as(assertCompletes)
      }
    ) @@ TestAspect.sequential
}
