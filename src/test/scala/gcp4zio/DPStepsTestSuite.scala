package gcp4zio

import com.google.cloud.dataproc.v1.Job
import zio.blocking.Blocking
import zio.stream.ZTransducer
import zio.{UIO, ZIO}
import zio.test.Assertion.equalTo
import zio.test._
import java.net.URI

object DPStepsTestSuite extends TestHelper {

  def printGcsLogs(response: Job): ZIO[GCSEnv with Blocking, Throwable, Unit] = {
    val uri    = new URI(response.getDriverOutputResourceUri)
    val bucket = uri.getHost
    val path   = uri.getPath.substring(1)
    GCSApi
      .listObjects(bucket, Some(path), recursive = false, List.empty)
      .flatMap { blob =>
        logger.info(s"Reading logs from gs://$bucket/${blob.getName}")
        GCSApi
          .getObject(bucket, blob.getName, 4096)
          .transduce(ZTransducer.utf8Decode >>> ZTransducer.splitLines)
          .tap(line => UIO(logger.info(line)))
      }
      .runDrain
  }

  val spec: ZSpec[environment.TestEnvironment with DPJobEnv with GCSEnv, Any] =
    suite("Dataproc Job APIs")(
      testM("executeHiveJob") {
        val step = DPJobApi
          .executeHiveJob("SELECT 1 AS ONE", dpCluster, gcpProjectId.get, gcpRegion.get)
          .flatMap(printGcsLogs)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("executeSparkJob") {
        val libs      = List("file:///usr/lib/spark/examples/jars/spark-examples.jar")
        val conf      = Map("spark.executor.memory" -> "1g", "spark.driver.memory" -> "1g")
        val mainClass = "org.apache.spark.examples.SparkPi"
        val step = DPJobApi
          .executeSparkJob(List("1000"), mainClass, libs, conf, dpCluster, gcpProjectId.get, gcpRegion.get)
          .flatMap(printGcsLogs)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      }
    ) @@ TestAspect.sequential
}
