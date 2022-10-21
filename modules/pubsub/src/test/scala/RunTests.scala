import zio.test._
//import gcp4zio.pubsub.{PubSubSubscriptionLive, PubSubTopicLive}
//import gcp4zio.{PubSubSubscriptionTestSuite, PubSubTopicTestSuite}

object RunTests extends ZIOSpecDefault {

  //private val env = PubSubTopicLive() ++ PubSubSubscriptionLive()

  override def spec: Spec[TestEnvironment, Any] = suite("PubSubTopicAndSubscription APIs")(
    //PubSubTopicTestSuite.spec,
    //PubSubSubscriptionTestSuite.spec
  ) @@ TestAspect.sequential //.provideShared(env.orDie)
}
