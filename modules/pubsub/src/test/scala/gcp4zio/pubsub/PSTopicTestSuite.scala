package gcp4zio.pubsub

import gcp4zio.Global._
import gcp4zio.pubsub.topic.PSTopic
import zio.ZIO
import zio.test.Assertion.containsString
import zio.test.{assertZIO, suite, test, Spec, TestAspect}

object PSTopicTestSuite {
  val spec: Spec[PSTopic, Any] =
    suite("PubSub Topic APIs")(
      test("Create Duplicate Topic within the project") {
        val step  = PSTopic.createTopic(gcsProject, topic1)
        val error = "ALREADY_EXISTS"
        assertZIO(step.foldZIO(ex => ZIO.succeed(ex.getMessage), _ => ZIO.fail("ok")))(containsString(error))
      },
      test("Delete not existing Topic") {
        val step  = PSTopic.deleteTopic(gcsProject, nonExistingTopic)
        val error = "NOT_FOUND"
        assertZIO(step.foldZIO(ex => ZIO.succeed(ex.getMessage), _ => ZIO.fail("ok")))(containsString(error))
      }
    ) @@ TestAspect.sequential
}
