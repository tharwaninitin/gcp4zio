package gcp4zio

import com.google.cloud.dataproc.v1.Job
import gcp4zio.dp.{DPJobApi, DPJobEnv}
import gcp4zio.gcs.{GCSApi, GCSEnv}
import zio.ZIO
import zio.stream.ZPipeline
import zio.test.Assertion.equalTo
import zio.test._
import java.net.URI

object DPStepsTestSuite extends TestHelper {

  def printGcsLogs(response: Job): ZIO[GCSEnv, Throwable, Unit] = {
    val uri    = new URI(response.getDriverOutputResourceUri)
    val bucket = uri.getHost
    val path   = uri.getPath.substring(1)
    GCSApi
      .listObjects(bucket, Some(path), recursive = false, List.empty)
      .flatMap { blob =>
        logger.info(s"Reading logs from gs://$bucket/${blob.getName} with size ${blob.getSize} bytes")
        GCSApi
          .getObject(bucket, blob.getName, 4096)
          .via(ZPipeline.utf8Decode >>> ZPipeline.splitLines)
          .tap(line => ZIO.succeed(logger.info(line)))
      }
      .runDrain
  }

  val spec: Spec[TestEnvironment with DPJobEnv with GCSEnv, Any] =
    suite("Dataproc Job APIs")(
      test("Run HiveJob") {
        val task = for {
          job <- DPJobApi.submitHiveJob("SELECT 1 AS ONE", dpCluster, gcpProject, gcpRegion)
          _   <- DPJobApi.trackJobProgress(gcpProject, gcpRegion, job)
          _   <- printGcsLogs(job)
        } yield ()
        assertZIO(task.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Run HiveJob (Failure)") {
        val task = for {
          job <- DPJobApi.submitHiveJob("SELE 1 AS ONE", dpCluster, gcpProject, gcpRegion)
          _ <- DPJobApi
            .trackJobProgress(gcpProject, gcpRegion, job)
            .tapError(_ => printGcsLogs(job))
        } yield ()
        assertZIO(task.foldZIO(_ => ZIO.succeed("ok"), _ => ZIO.fail("Defect!!!!")))(equalTo("ok"))
      },
      test("Run SparkJob") {
        val libs      = List("file:///usr/lib/spark/examples/jars/spark-examples.jar")
        val conf      = Map("spark.executor.memory" -> "1g", "spark.driver.memory" -> "1g")
        val mainClass = "org.apache.spark.examples.SparkPi"
        val task = for {
          job <- DPJobApi.submitSparkJob(List("1000"), mainClass, libs, conf, dpCluster, gcpProject, gcpRegion)
          _   <- DPJobApi.trackJobProgress(gcpProject, gcpRegion, job)
          _   <- printGcsLogs(job)
        } yield ()
        assertZIO(task.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      }
    ) @@ TestAspect.sequential
}
