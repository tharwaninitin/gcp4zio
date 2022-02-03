package gcp4zio

import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

object DPStepsTestSuite {
  val spec: ZSpec[environment.TestEnvironment, Any] =
    (suite("EtlFlow DPJobSteps")(
      testM("Execute DPHiveJob step") {
        val step =
          DPJobApi.executeHiveJob("SELECT 1 AS ONE", sys.env("DP_CLUSTER_NAME"), sys.env("DP_PROJECT_ID"), sys.env("DP_REGION"))
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute DPSparkJob step") {
        val libs = List("file:///usr/lib/spark/examples/jars/spark-examples.jar")
        val conf = Map(
          "spark.executor.memory" -> "1g",
          "spark.driver.memory"   -> "1g"
        )
        val step = DPJobApi.executeSparkJob(
          List("1000"),
          "org.apache.spark.examples.SparkPi",
          libs,
          conf,
          sys.env("DP_CLUSTER_NAME"),
          sys.env("DP_PROJECT_ID"),
          sys.env("DP_REGION")
        )
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      }
    ) @@ TestAspect.sequential).provideCustomLayerShared(DPJob.live(sys.env("DP_ENDPOINT")).orDie)
}
