import zio.test._
import gcp4zio.pubsub._
import gcp4zio._

object RunTests extends ZIOSpecDefault {

  private val env = PSTopic.test ++ PSSub.test

  override def spec: Spec[TestEnvironment, Any] = (suite("PubSub Topic/Subscription APIs")(
    PSTopicCreateTestSuite.spec,
    PSTopicTestSuite.spec,
    PSSubTestSuite.spec,
    PSTopicDeleteTestSuite.spec
  ) @@ TestAspect.sequential).provideShared(env.orDie)
}
