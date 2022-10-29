package gcp4zio.pubsub

import gcp4zio.Global._
import gcp4zio.pubsub.topic.PSTopic
import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

@SuppressWarnings(Array("org.wartremover.warts.ToString"))
object PSTopicDeleteTestSuite {
  val spec: Spec[PSTopic, Any] = test("Delete Topic") {
    val step = PSTopic.deleteTopic(gcsProject, topic1)
    assertZIO(step.foldZIO(ex => ZIO.fail(ex.toString), _ => ZIO.succeed("ok")))(equalTo("ok"))
  }
}
