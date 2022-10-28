import zio.test._

object RunTests extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment, Any] = suite("Cloud Monitoring APIs")(
    //MonitoringTestSuite.spec,
  ) @@ TestAspect.sequential //).provideLayerShared(Monitoring.live().orDie)
}
