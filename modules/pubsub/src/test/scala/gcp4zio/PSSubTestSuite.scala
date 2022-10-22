package gcp4zio

import gcp4zio.Global.{gcsProject, notValidTopic, subscription1, subscription2, topic}
import gcp4zio.pubsub.{PSSubApi, PSSubEnv}
import zio.ZIO
import zio.test.Assertion.{containsString, equalTo}
import zio.test.{assertZIO, suite, test, Spec, TestAspect}

@SuppressWarnings(Array("org.wartremover.warts.AutoUnboxing"))
object PSSubTestSuite {
  val spec: Spec[PSSubEnv, Any] =
    suite("PubSubSubscription APIs")(
      test("Create Subscription with existing topic") {
        val step = PSSubApi.createSubscription(gcsProject, subscription1, topic)
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.toString), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Create Subscription with not existing topic") {
        val step  = PSSubApi.createSubscription(gcsProject, subscription2, notValidTopic)
        val error = "NOT_FOUND"
        assertZIO(step.foldZIO(ex => ZIO.succeed(ex.getMessage), _ => ZIO.fail("ok")))(containsString(error))
      },
      test("Create Subscription with existing Subscription Name") {
        val step  = PSSubApi.createSubscription(gcsProject, subscription1, topic)
        val error = "ALREADY_EXISTS"
        assertZIO(step.foldZIO(ex => ZIO.succeed(ex.getMessage), _ => ZIO.fail("ok")))(containsString(error))
      },
      test("Delete existing Subscription") {
        val step = PSSubApi.deleteSubscription(gcsProject, subscription1)
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.toString), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Delete not existing Subscription") {
        val step  = PSSubApi.deleteSubscription(gcsProject, subscription1)
        val error = "NOT_FOUND"
        assertZIO(step.foldZIO(ex => ZIO.succeed(ex.getMessage), _ => ZIO.fail("ok")))(containsString(error))
      }
    ) @@ TestAspect.sequential
}