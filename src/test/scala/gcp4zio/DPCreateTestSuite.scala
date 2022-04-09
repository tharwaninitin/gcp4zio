package gcp4zio

import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

object DPCreateTestSuite extends TestHelper {
  val spec: ZSpec[environment.TestEnvironment with DPEnv, Any] =
    testM("Execute DPCreateStep") {
      val dpProps = ClusterProps(
        bucketName = dpBucket,
        subnetUri = dpSubnetUri,
        networkTags = dpNetworkTags,
        serviceAccount = dpServiceAccount
      )
      val step = DPApi.createDataproc(dpCluster, gcpProjectId.getOrElse("NA"), gcpRegion.getOrElse("NA"), dpProps)
      assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    }
}
