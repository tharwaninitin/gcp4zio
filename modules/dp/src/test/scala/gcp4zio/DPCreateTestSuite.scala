package gcp4zio

import gcp4zio.Global._
import gcp4zio.dp.{ClusterProps, DPApi, DPEnv}
import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

object DPCreateTestSuite {
  val spec: Spec[TestEnvironment with DPEnv, Any] =
    test("Execute DPCreateStep") {
      val dpProps = ClusterProps(
        bucketName = dpBucket,
        subnetUri = dpSubnetUri,
        networkTags = dpNetworkTags,
        serviceAccount = dpServiceAccount
      )
      val step = DPApi.createDataproc(dpCluster, gcpProject, gcpRegion, dpProps)
      assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    }
}
