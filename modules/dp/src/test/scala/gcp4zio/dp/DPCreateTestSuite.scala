package gcp4zio.dp

import gcp4zio.Global._
import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test.{assertZIO, test, Spec}

object DPCreateTestSuite {
  val spec: Spec[DPCluster, Any] =
    test("Execute DPCreateStep") {
      val dpProps = ClusterProps(
        bucketName = dpBucket,
        subnetUri = dpSubnetUri,
        networkTags = dpNetworkTags,
        serviceAccount = dpServiceAccount
      )
      val step = DPCluster.createDataproc(dpCluster, dpProps)
      assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    }
}
