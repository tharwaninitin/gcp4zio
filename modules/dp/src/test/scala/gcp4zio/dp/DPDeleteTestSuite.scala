package gcp4zio.dp

import gcp4zio.Global.{dpCluster, gcpProject, gcpRegion}
import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test.{assertZIO, test, Spec}

object DPDeleteTestSuite {
  val spec: Spec[DPCluster, Any] =
    test("Execute DPDeleteStep") {
      val step = DPCluster.deleteDataproc(dpCluster, gcpProject, gcpRegion)
      assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    }
}
