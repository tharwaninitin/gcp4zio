package gcp4zio

import gcp4zio.Global._
import gcp4zio.pubsub.{PSTopicApi, PSTopicEnv}
import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

object PSTopicCreateTestSuite {
  val spec: Spec[PSTopicEnv, Any] = test("Create Topic") {
    val step = PSTopicApi.createTopic(gcsProject, topic)
    assertZIO(step.foldZIO(ex => ZIO.fail(ex.toString), _ => ZIO.succeed("ok")))(equalTo("ok"))
  }
}
