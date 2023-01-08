package gcp4zio.dp

import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test.{assertZIO, suite, test, Spec, TestAspect}

object DPStepsTestSuite {
  val spec: Spec[DPJob, Any] =
    suite("Dataproc Job APIs")(
      test("Run HiveJob") {
        val task = for {
          job <- DPJob.submitHiveJob("SELECT 1 AS ONE")
          _   <- DPJob.trackJobProgress(job)
        } yield ()
        assertZIO(task.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Run HiveJob (Failure)") {
        val task = for {
          job <- DPJob.submitHiveJob("SELE 1 AS ONE")
          _   <- DPJob.trackJobProgress(job)
        } yield ()
        assertZIO(task.foldZIO(_ => ZIO.succeed("ok"), _ => ZIO.fail("Defect!!!!")))(equalTo("ok"))
      },
      test("Run SparkJob") {
        val libs      = List("file:///usr/lib/spark/examples/jars/spark-examples.jar")
        val conf      = Map("spark.executor.memory" -> "1g", "spark.driver.memory" -> "1g")
        val mainClass = "org.apache.spark.examples.SparkPi"
        val task = for {
          job <- DPJob.submitSparkJob(List("1000"), mainClass, libs, conf)
          _   <- DPJob.trackJobProgress(job)
        } yield ()
        assertZIO(task.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      }
    ) @@ TestAspect.sequential
}
