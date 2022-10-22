package gcp4zio
package pubsub

import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.pubsub.v1.{PushConfig, Subscription, SubscriptionName, TopicName}
import zio._

@SuppressWarnings(Array("org.wartremover.warts.ToString"))
case class PSSubLive(subscriptionClient: SubscriptionAdminClient) extends PSSubApi[Task] {

  override def createSubscription(
      project: String,
      subscription: String,
      topic: String,
      ackDeadlineSeconds: RuntimeFlags
  ): Task[Subscription] = ZIO.attempt {
    val topicName        = TopicName.of(project, topic)
    val subscriptionName = SubscriptionName.of(project, subscription)
    subscriptionClient.createSubscription(subscriptionName, topicName, PushConfig.getDefaultInstance, ackDeadlineSeconds)
  }

  override def deleteSubscription(projectId: String, subscriptionId: String): Task[Unit] = ZIO.attempt {
    val subscriptionName = SubscriptionName.of(projectId, subscriptionId)
    subscriptionClient.deleteSubscription(subscriptionName)
  }
}

object PSSubLive {
  def apply(path: Option[String] = None): TaskLayer[PSSubEnv] =
    ZLayer.scoped(ZIO.fromAutoCloseable(PSSubClient(path)).map(client => PSSubLive(client)))
}
