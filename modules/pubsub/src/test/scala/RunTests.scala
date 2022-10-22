import zio.test._
import gcp4zio.pubsub.{PSSubLive, PSTopicLive}
import gcp4zio.{PSSubTestSuite, PSTopicTestSuite}

object RunTests extends ZIOSpecDefault {

  private val env = PSTopicLive() ++ PSSubLive()

  override def spec: Spec[TestEnvironment, Any] = (suite("PubSubTopicAndSubscription APIs")(
    PSTopicTestSuite.spec,
    PSSubTestSuite.spec
  ) @@ TestAspect.sequential).provideShared(env.orDie)
}
