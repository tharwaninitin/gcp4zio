import gcp4zio.gcs.{GCS, GCSCopyTestSuite, GCSTestSuite, PSNotificationTestSuite}
import zio.test._

object RunTests extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment, Any] = (suite("GCS Apis")(
    GCSTestSuite.spec,
    GCSCopyTestSuite.spec,
    PSNotificationTestSuite.spec
  ) @@ TestAspect.sequential).provideLayerShared(GCS.live().orDie)
}
