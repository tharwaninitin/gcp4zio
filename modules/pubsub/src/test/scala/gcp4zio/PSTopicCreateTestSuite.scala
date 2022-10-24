package gcp4zio

import gcp4zio.Global._
import gcp4zio.pubsub.PSTopicEnv
import gcp4zio.pubsub.topic.PSTopic
import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

@SuppressWarnings(Array("org.wartremover.warts.ToString"))
object PSTopicCreateTestSuite {
  val spec: Spec[PSTopicEnv, Any] = test("Create Topic") {
    val step = PSTopic.createTopic(gcsProject, topic)
    assertZIO(step.foldZIO(ex => ZIO.fail(ex.toString), _ => ZIO.succeed("ok")))(equalTo("ok"))
  }
}
