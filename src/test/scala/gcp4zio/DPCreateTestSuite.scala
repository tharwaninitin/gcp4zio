package gcp4zio

import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

object DPCreateTestSuite extends TestHelper {
  val spec: ZSpec[environment.TestEnvironment with DPEnv, Any] =
    testM("Execute DPCreateStep") {
      val dpProps = DataprocProperties(
        bucket_name = dp_bucket_name,
        subnet_uri = dp_subnet_uri,
        network_tags = dp_network_tags,
        service_account = dp_service_account
      )
      val step = DPApi.createDataproc(dp_cluster_name, gcp_project_id.get, gcp_region.get, dpProps)
      assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    }
}
