package gcp4zio

import gcp4zio.Global._
import gcp4zio.pubsub.{PSTopicApi, PSTopicEnv}
import zio.ZIO
import zio.test.Assertion.containsString
import zio.test._

object PSTopicTestSuite {
  val spec: Spec[PSTopicEnv, Any] =
    suite("PubSub Topic APIs")(
      test("Create Duplicate Topic within the project") {
        val step  = PSTopicApi.createTopic(gcsProject, topic)
        val error = "ALREADY_EXISTS"
        assertZIO(step.foldZIO(ex => ZIO.succeed(ex.getMessage), _ => ZIO.fail("ok")))(containsString(error))
      },
      test("Delete not existing Topic") {
        val step  = PSTopicApi.deleteTopic(gcsProject, nonExistingTopic)
        val error = "NOT_FOUND"
        assertZIO(step.foldZIO(ex => ZIO.succeed(ex.getMessage), _ => ZIO.fail("ok")))(containsString(error))
      }
    ) @@ TestAspect.sequential
}
