package gcp4zio.pubsub.subscription

import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.pubsub.v1._
import zio._

@SuppressWarnings(Array("org.wartremover.warts.ToString"))
case class PSSubscriptionImpl(client: SubscriptionAdminClient) extends PSSubscription {

  override def createPullSubscription(
      project: String,
      subscription: String,
      topic: String,
      ackDeadlineSeconds: Int
  ): Task[Subscription] = ZIO.attempt {
    val topicName        = TopicName.of(project, topic)
    val subscriptionName = SubscriptionName.of(project, subscription)
    client.createSubscription(subscriptionName, topicName, PushConfig.getDefaultInstance, ackDeadlineSeconds)
  }

  override def createPushSubscription(
      project: String,
      subscription: String,
      topic: String,
      ackDeadlineSeconds: Int,
      pushEndpoint: String
  ): Task[Subscription] = ZIO.attempt {
    val topicName        = TopicName.of(project, topic)
    val subscriptionName = SubscriptionName.of(project, subscription)
    val pushConfig       = PushConfig.newBuilder.setPushEndpoint(pushEndpoint).build
    client.createSubscription(subscriptionName, topicName, pushConfig, ackDeadlineSeconds)
  }

  override def createBQSubscription(
      project: String,
      subscription: String,
      topic: String,
      bqTableId: String
  ): Task[Subscription] = ZIO.attempt {
    val topicName        = TopicName.of(project, topic)
    val subscriptionName = SubscriptionName.of(project, subscription)
    val bigqueryConfig   = BigQueryConfig.newBuilder().setTable(bqTableId).setWriteMetadata(true).build()
    val bqSubscription = Subscription
      .newBuilder()
      .setName(subscriptionName.toString)
      .setTopic(topicName.toString)
      .setBigqueryConfig(bigqueryConfig)
      .build()
    client.createSubscription(bqSubscription)
  }

  override def deleteSubscription(project: String, subscription: String): Task[Unit] = ZIO.attempt {
    val subscriptionName = SubscriptionName.of(project, subscription)
    client.deleteSubscription(subscriptionName)
  }
}
