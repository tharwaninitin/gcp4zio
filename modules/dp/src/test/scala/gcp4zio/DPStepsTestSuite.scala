package gcp4zio

import gcp4zio.Global._
import gcp4zio.dp.{DPJobApi, DPJobEnv}
import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

object DPStepsTestSuite {

  val spec: Spec[TestEnvironment with DPJobEnv, Any] =
    suite("Dataproc Job APIs")(
      test("Run HiveJob") {
        val task = for {
          job <- DPJobApi.submitHiveJob("SELECT 1 AS ONE", dpCluster, gcpProject, gcpRegion)
          _   <- DPJobApi.trackJobProgress(gcpProject, gcpRegion, job)
        } yield ()
        assertZIO(task.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Run HiveJob (Failure)") {
        val task = for {
          job <- DPJobApi.submitHiveJob("SELE 1 AS ONE", dpCluster, gcpProject, gcpRegion)
          _   <- DPJobApi.trackJobProgress(gcpProject, gcpRegion, job)
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
        } yield ()
        assertZIO(task.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      }
    ) @@ TestAspect.sequential
}
