package gcp4zio

import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

object DPCreateTestSuite extends TestHelper {
  val spec: ZSpec[environment.TestEnvironment with DPEnv, Any] =
    testM("Execute DPCreateStep") {
      val dpProps = DataprocProperties(
        bucket_name = dpBucket,
        subnet_uri = dpSubnetUri,
        network_tags = dpNetworkTags,
        service_account = dpServiceAccount
      )
      val step = DPApi.createDataproc(dpCluster, gcpProjectId.getOrElse("NA"), gcpRegion.getOrElse("NA"), dpProps)
      assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    }
}
