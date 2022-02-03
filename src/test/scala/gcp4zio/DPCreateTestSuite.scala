package gcp4zio

import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

object DPCreateTestSuite {
  val spec: ZSpec[environment.TestEnvironment with DPEnv, Any] =
    testM("Execute DPCreateStep") {
      val dpProps = DataprocProperties(
        bucket_name = sys.env("DP_BUCKET_NAME"),
        subnet_uri = sys.env.get("DP_SUBNET_WORK_URI"),
        network_tags = sys.env.getOrElse("DP_NETWORK_TAGS", "").split(",").toList,
        service_account = sys.env.get("DP_SERVICE_ACCOUNT")
      )
      val step = DPApi
        .createDataproc(sys.env("DP_CLUSTER_NAME"), sys.env("DP_PROJECT_ID"), sys.env("DP_REGION"), dpProps)
        .provideLayer(DP.live(sys.env("DP_ENDPOINT")).orDie)
      assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    }
}
