package gcp4zio
package pubsub

import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.pubsub.v1.{Topic, TopicName}
import zio._

@SuppressWarnings(Array("org.wartremover.warts.ToString"))
case class PSTopicLive(client: TopicAdminClient) extends PSTopicApi[Task] {

  override def createTopic(projectId: String, topicId: String): Task[Topic] = ZIO.attempt {
    val topicName = TopicName.of(projectId, topicId)
    client.createTopic(topicName)
  }

  override def deleteTopic(project: String, topic: String): Task[Unit] = ZIO.attempt {
    val topicName = TopicName.of(project, topic)
    client.deleteTopic(topicName)
  }
}

object PSTopicLive {
  def apply(path: Option[String] = None): TaskLayer[PSTopicEnv] =
    ZLayer.scoped(ZIO.fromAutoCloseable(PSTopicClient(path)).map(client => PSTopicLive(client)))
}
