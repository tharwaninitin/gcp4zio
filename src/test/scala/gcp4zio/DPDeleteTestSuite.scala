package gcp4zio

import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

object DPDeleteTestSuite extends TestHelper {
  val spec: ZSpec[environment.TestEnvironment with DPEnv, Any] =
    testM("Execute DPDeleteStep") {
      val step = DPApi.deleteDataproc(dp_cluster_name, gcp_project_id.get, gcp_region.get)
      assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    }
}
