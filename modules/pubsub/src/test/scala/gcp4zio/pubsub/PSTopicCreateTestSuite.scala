package gcp4zio.pubsub

import gcp4zio.Global._
import gcp4zio.pubsub.topic.PSTopic
import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test.{assertZIO, suite, test, Spec}

@SuppressWarnings(Array("org.wartremover.warts.ToString"))
object PSTopicCreateTestSuite {
  val spec: Spec[PSTopic, Any] =
    suite("PubSub Topic APIs")(
      test("Create Topic") {
        val step = PSTopic.createTopic(gcsProject, topic1)
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.toString), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Add IAM Policy Binding to Topic") {
        val step = PSTopic.addIAMPolicyBindingToTopic(gcsProject, topic1, member, role)
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.toString), _ => ZIO.succeed("ok")))(equalTo("ok"))
      }
    )
}
