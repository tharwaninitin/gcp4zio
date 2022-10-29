package gcp4zio.pubsub

import gcp4zio.Global._
import gcp4zio.pubsub.subscription.PSSubscription
import zio.ZIO
import zio.test.Assertion.{containsString, equalTo}
import zio.test._

@SuppressWarnings(Array("org.wartremover.warts.AutoUnboxing"))
object PSSubTestSuite {
  val spec: Spec[PSSubscription, Any] =
    suite("PubSubSubscription APIs")(
      test("Create Subscription with existing topic") {
        val step = PSSubscription.createPullSubscription(gcsProject, subscription1, topic1)
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.toString), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Create Subscription with non-existing topic") {
        val step  = PSSubscription.createPullSubscription(gcsProject, subscription2, nonExistingTopic)
        val error = "NOT_FOUND"
        assertZIO(step.foldZIO(ex => ZIO.succeed(ex.getMessage), _ => ZIO.fail("ok")))(containsString(error))
      },
      test("Create Subscription with existing Subscription Name") {
        val step  = PSSubscription.createPullSubscription(gcsProject, subscription1, topic1)
        val error = "ALREADY_EXISTS"
        assertZIO(step.foldZIO(ex => ZIO.succeed(ex.getMessage), _ => ZIO.fail("ok")))(containsString(error))
      },
      test("Delete existing Subscription") {
        val step = PSSubscription.deleteSubscription(gcsProject, subscription1)
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.toString), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Delete non-existing Subscription") {
        val step  = PSSubscription.deleteSubscription(gcsProject, subscription1)
        val error = "NOT_FOUND"
        assertZIO(step.foldZIO(ex => ZIO.succeed(ex.getMessage), _ => ZIO.fail("ok")))(containsString(error))
      }
    ) @@ TestAspect.sequential
}
