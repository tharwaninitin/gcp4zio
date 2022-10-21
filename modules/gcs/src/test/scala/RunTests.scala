import zio.test._
//import gcp4zio.gcs.GCSLive

object RunTests extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment, Any] = suite("GCS Apis")(
    // GCSTestSuite.spec,
    // GCSCopyTestSuite.spec,
    // GCSNotificationConfigurationTestSuite.spec
  ) @@ TestAspect.sequential // .provideLayerShared(GCSLive().orDie)
}
