import gcp4zio.pubsub.subscription.PSSubscription
import gcp4zio.pubsub.topic.PSTopic
import gcp4zio.pubsub.{PSSubTestSuite, PSTopicCreateTestSuite, PSTopicDeleteTestSuite, PSTopicTestSuite}
import zio.test._

object RunTests extends ZIOSpecDefault {

  private val env = PSTopic.test ++ PSSubscription.test

  override def spec: Spec[TestEnvironment, Any] = (suite("PubSub Topic/Subscription APIs")(
    PSTopicCreateTestSuite.spec,
    PSTopicTestSuite.spec,
    PSSubTestSuite.spec,
    PSTopicDeleteTestSuite.spec
  ) @@ TestAspect.sequential).provideShared(env.orDie)
}
