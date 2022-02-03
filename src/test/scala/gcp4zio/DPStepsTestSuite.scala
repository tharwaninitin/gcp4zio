package gcp4zio

import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

object DPStepsTestSuite extends TestHelper {
  val spec: ZSpec[environment.TestEnvironment with DPJobEnv, Any] =
    suite("EtlFlow DPJobSteps")(
      testM("Execute DPHiveJob step") {
        val step =
          DPJobApi.executeHiveJob("SELECT 1 AS ONE", dp_cluster_name, gcp_project_id.get, gcp_region.get)
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      testM("Execute DPSparkJob step") {
        val libs = List("file:///usr/lib/spark/examples/jars/spark-examples.jar")
        val conf = Map("spark.executor.memory" -> "1g", "spark.driver.memory" -> "1g")
        val step = DPJobApi.executeSparkJob(
          List("1000"),
          "org.apache.spark.examples.SparkPi",
          libs,
          conf,
          dp_cluster_name,
          gcp_project_id.get,
          gcp_region.get
        )
        assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      }
    ) @@ TestAspect.sequential
}
