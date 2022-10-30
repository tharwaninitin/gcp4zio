package gcp4zio.dp

import gcp4zio.Global.{dpCluster, gcpProject, gcpRegion}
import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test.{assertZIO, suite, test, Spec, TestAspect}

object DPStepsTestSuite {
  val spec: Spec[DPJob, Any] =
    suite("Dataproc Job APIs")(
      test("Run HiveJob") {
        val task = for {
          job <- DPJob.submitHiveJob("SELECT 1 AS ONE", dpCluster, gcpProject, gcpRegion)
          _   <- DPJob.trackJobProgress(gcpProject, gcpRegion, job)
        } yield ()
        assertZIO(task.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Run HiveJob (Failure)") {
        val task = for {
          job <- DPJob.submitHiveJob("SELE 1 AS ONE", dpCluster, gcpProject, gcpRegion)
          _   <- DPJob.trackJobProgress(gcpProject, gcpRegion, job)
        } yield ()
        assertZIO(task.foldZIO(_ => ZIO.succeed("ok"), _ => ZIO.fail("Defect!!!!")))(equalTo("ok"))
      },
      test("Run SparkJob") {
        val libs      = List("file:///usr/lib/spark/examples/jars/spark-examples.jar")
        val conf      = Map("spark.executor.memory" -> "1g", "spark.driver.memory" -> "1g")
        val mainClass = "org.apache.spark.examples.SparkPi"
        val task = for {
          job <- DPJob.submitSparkJob(List("1000"), mainClass, libs, conf, dpCluster, gcpProject, gcpRegion)
          _   <- DPJob.trackJobProgress(gcpProject, gcpRegion, job)
        } yield ()
        assertZIO(task.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      }
    ) @@ TestAspect.sequential
}
