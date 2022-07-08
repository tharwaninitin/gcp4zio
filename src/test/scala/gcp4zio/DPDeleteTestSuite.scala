package gcp4zio

import gcp4zio.dp.{DPApi, DPEnv}
import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

object DPDeleteTestSuite extends TestHelper {
  val spec: Spec[TestEnvironment with DPEnv, Any] =
    test("Execute DPDeleteStep") {
      val step = DPApi.deleteDataproc(dpCluster, gcpProject, gcpRegion)
      assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    }
}
