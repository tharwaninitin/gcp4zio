package gcp4zio

import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

object DPDeleteTestSuite {
  val spec: ZSpec[environment.TestEnvironment, Any] =
    testM("Execute DPDeleteStep") {
      val step = DPApi
        .deleteDataproc(sys.env("DP_CLUSTER_NAME"), sys.env("DP_PROJECT_ID"), sys.env("DP_REGION"))
        .provideLayer(DP.live(sys.env("DP_ENDPOINT")).orDie)
      assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    }
}
