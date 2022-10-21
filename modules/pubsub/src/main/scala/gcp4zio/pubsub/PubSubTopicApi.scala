package gcp4zio
package pubsub

import com.google.pubsub.v1.Topic
import zio._

trait PubSubTopicApi[F[_]] {
  def createTopic(project: String, topic: String): F[Topic]
  def deleteTopic(project: String, topic: String): F[Unit]
}

object PubSubTopicApi {
  def createTopic(project: String, topic: String): ZIO[PubSubTopicEnv, Throwable, Topic] =
    ZIO.environmentWithZIO(_.get.createTopic(project, topic))
  def deleteTopic(project: String, topic: String): ZIO[PubSubTopicEnv, Throwable, Unit] =
    ZIO.environmentWithZIO(_.get.deleteTopic(project, topic))
}
