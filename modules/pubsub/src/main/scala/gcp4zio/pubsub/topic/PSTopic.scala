package gcp4zio.pubsub.topic

import com.google.pubsub.v1.Topic
import gcp4zio.pubsub.PSTopicEnv
import zio._

trait PSTopic[F[_]] {
  def createTopic(project: String, topic: String): F[Topic]
  def deleteTopic(project: String, topic: String): F[Unit]
}

object PSTopic {
  def createTopic(project: String, topic: String): ZIO[PSTopicEnv, Throwable, Topic] =
    ZIO.environmentWithZIO(_.get.createTopic(project, topic))
  def deleteTopic(project: String, topic: String): ZIO[PSTopicEnv, Throwable, Unit] =
    ZIO.environmentWithZIO(_.get.deleteTopic(project, topic))
  def live(path: Option[String] = None): TaskLayer[PSTopicEnv] =
    ZLayer.scoped(ZIO.fromAutoCloseable(PSTopicClient(path)).map(client => PSTopicImpl(client)))
  val test: TaskLayer[PSTopicEnv] =
    ZLayer.scoped(ZIO.fromAutoCloseable(PSTopicClient.testClient).map(client => PSTopicImpl(client)))
}
