package gcp4zio
package pubsub

import com.google.pubsub.v1.Subscription
import zio._

trait PubSubSubscriptionApi[F[_]] {
  def createSubscription(project: String, subscription: String, topic: String, ackDeadlineSeconds: Int) : F[Subscription]
  def deleteSubscription(projectId: String, subscriptionId: String): F[Unit]
}

object PubSubSubscriptionApi  {
  def createSubscription(project: String, subscription: String, topic: String, ackDeadlineSeconds: Int = 10): ZIO[PubSubSubscriptionEnv, Throwable, Subscription] =
    ZIO.environmentWithZIO(_.get.createSubscription(project, subscription, topic, ackDeadlineSeconds))
  def deleteSubscription(projectId: String, subscriptionId: String): ZIO[PubSubSubscriptionEnv, Throwable, Unit] =
    ZIO.environmentWithZIO(_.get.deleteSubscription(projectId, subscriptionId))
}
