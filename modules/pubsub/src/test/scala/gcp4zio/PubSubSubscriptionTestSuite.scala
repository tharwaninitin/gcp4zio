package gcp4zio

import gcp4zio.Global.gcsProject
import gcp4zio.pubsub.{PubSubSubscriptionApi, PubSubSubscriptionEnv}
import gcp4zio.utils.ApplicationLogger
import zio.ZIO
import zio.test.Assertion.{containsString, equalTo}
import zio.test.{Spec, TestAspect, assertZIO, suite, test}


@SuppressWarnings(Array("org.wartremover.warts.AutoUnboxing"))
object PubSubSubscriptionTestSuite extends ApplicationLogger {
  val validTopic = "order_topic"
  val notValidTopic = "notExistingTopic"
  val subscription = "order_topic-sub"
  val spec: Spec[PubSubSubscriptionEnv, Any] =
    suite("PubSubSubscription APIs")(
      test("Create Subscription with existing topic") {
        val step = PubSubSubscriptionApi.createSubscription(gcsProject, subscription, validTopic)
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.toString), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Create Subscription with not existing topic") {
        val step = PubSubSubscriptionApi.createSubscription(gcsProject, subscription + "1", notValidTopic)
        val error = "NOT_FOUND"
        assertZIO(step.foldZIO(ex => ZIO.succeed(ex.getMessage), _ => ZIO.fail("ok")))(containsString(error))
      },
      test("Create Subscription with existing Subscription Name") {
        val step = PubSubSubscriptionApi.createSubscription(gcsProject, subscription, validTopic)
        val error = "ALREADY_EXISTS"
        assertZIO(step.foldZIO(ex => ZIO.succeed(ex.getMessage), _ => ZIO.fail("ok")))(containsString(error))
      },
      test("Delete existing Subscription") {
        val step = PubSubSubscriptionApi.deleteSubscription(gcsProject, subscription)
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.toString), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Delete not existing Subscription") {
        val step = PubSubSubscriptionApi.deleteSubscription(gcsProject, subscription)
        val error = "NOT_FOUND"
        assertZIO(step.foldZIO(ex => ZIO.succeed(ex.getMessage), _ => ZIO.fail("ok")))(containsString(error))
      }
    ) @@ TestAspect.sequential
}
