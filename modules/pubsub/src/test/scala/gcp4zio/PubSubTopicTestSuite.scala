package gcp4zio

import gcp4zio.Global.gcsProject
import gcp4zio.pubsub.{PubSubTopicApi, PubSubTopicEnv}
import zio.ZIO
import zio.test.Assertion.{containsString, equalTo}
import zio.test._

object PubSubTopicTestSuite {
  val topic = "test-topic"
  val spec: Spec[PubSubTopicEnv, Any] =
    suite("PubSubTopic APIs")(
      test("Create Topic within the project defined") {
        val step = PubSubTopicApi.createTopic(gcsProject, topic)
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.toString), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Create Duplicate Topic within the project") {
        val step = PubSubTopicApi.createTopic(gcsProject, topic)
        val error = "ALREADY_EXISTS"
        assertZIO(step.foldZIO(ex => ZIO.succeed(ex.getMessage), _ => ZIO.fail("ok")))(containsString(error))
      },
      test("Delete existing Topic") {
        val step = PubSubTopicApi.deleteTopic(gcsProject, topic)
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.toString), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Delete not existing Topic") {
        val step = PubSubTopicApi.deleteTopic(gcsProject, topic)
        val error = "NOT_FOUND"
        assertZIO(step.foldZIO(ex => ZIO.succeed(ex.getMessage), _ => ZIO.fail("ok")))(containsString(error))
      }
    ) @@ TestAspect.sequential
}
