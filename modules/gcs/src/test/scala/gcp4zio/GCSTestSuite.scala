package gcp4zio

import com.google.cloud.storage.Storage.{BlobTargetOption, BlobWriteOption}
import gcp4zio.Global._
import gcp4zio.gcs.{GCSApi, GCSEnv}
import gcp4zio.utils.ApplicationLogger
import zio.ZIO
import zio.test.Assertion.equalTo
import zio.stream._
import zio.test._
import java.nio.charset.CharacterCodingException
import java.nio.file.Paths
import java.util.UUID

object GCSTestSuite extends ApplicationLogger {
  val prefix = "temp/test/ratings.csv"
  val spec: Spec[TestEnvironment with GCSEnv, Any] =
    suite("GCS Apis")(
      test("Execute putObject") {
        val path = Paths.get(filePathCsv)
        val step = GCSApi.putObject(gcsBucket, prefix, path, List.empty)
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Execute putObject with Overwrite Error") {
        val path  = Paths.get(filePathCsv)
        val opts  = List(BlobTargetOption.doesNotExist())
        val step  = GCSApi.putObject(gcsBucket, prefix, path, opts)
        val error = "At least one of the pre-conditions you specified did not hold."
        assertZIO(step.foldZIO(ex => ZIO.succeed(ex.getMessage), _ => ZIO.fail("ok")))(equalTo(error))
      },
      test("Execute lookupObject with existing object") {
        val step = GCSApi.lookupObject(gcsBucket, prefix)
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op.toString)))(equalTo("true"))
      },
      test("Execute lookupObject with non existing object") {
        val step = GCSApi.lookupObject(gcsBucket, UUID.randomUUID().toString)
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op.toString)))(equalTo("false"))
      },
      test("Execute(streaming) getObject") {
        val step = GCSApi
          .getObject(gcsBucket, prefix, 4096)
          .via(ZPipeline.utf8Decode >>> ZPipeline.splitLines)
          .tap(op => ZIO.succeed(logger.info(op)))
          .runDrain
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Execute(streaming) getObject => putObject") {
        val opts = List(BlobWriteOption.doesNotExist())
        val sink = GCSApi.putObject(gcsBucket, "temp/test/ratings2.csv", opts)
        val step = GCSApi.getObject(gcsBucket, prefix, 4096).run(sink)
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), op => ZIO.succeed(op.toString)))(equalTo("124"))
      },
      test("Execute delObject with existing object") {
        val effect = GCSApi.deleteObject(gcsBucket, "temp/test/ratings2.csv")
        assertZIO(effect.foldZIO(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op.toString)))(equalTo("true"))
      },
      test("Execute delObject with non existing object") {
        val effect = GCSApi.deleteObject(gcsBucket, "temp/test/ratings2.csv")
        assertZIO(effect.foldZIO(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op.toString)))(equalTo("false"))
      },
      test("Execute listObjects to get size of all objects in bucket") {
        val mb = 1024 * 1024
        val effect =
          GCSApi
            .listObjects(gcsBucket)
            .map(_.getSize)
            .runFold(0L)(_ + _)
            .tap(s => ZIO.succeed(logger.info(s"Total Size => ${s / mb} MB")))
        assertZIO(effect.foldZIO(ex => ZIO.fail(ex.toString), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Execute(streaming) listObjects => getObject") {
        val pipeline: ZPipeline[Any, CharacterCodingException, Byte, String] = ZPipeline.utf8Decode >>> ZPipeline.splitLines
        val logStream                                                        = ZStream.fromZIO(ZIO.succeed(logger.info("#" * 50)))
        val stream = GCSApi
          .listObjects(gcsBucket, Some("temp/test/"), recursive = false, List.empty)
          .flatMap { blob =>
            logger.info(blob.getName)
            GCSApi.getObject(gcsBucket, blob.getName, 4096).via(pipeline).tap(line => ZIO.succeed(logger.info(line)))
          }
        val effect = (logStream ++ stream ++ logStream).runDrain
        effect.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")).as(assertCompletes)
      }
    ) @@ TestAspect.sequential
}
