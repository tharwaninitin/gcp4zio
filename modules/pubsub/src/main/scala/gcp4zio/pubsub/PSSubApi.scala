package gcp4zio
package pubsub

import com.google.pubsub.v1.Subscription
import zio._

trait PSSubApi[F[_]] {
  def createSubscription(project: String, subscription: String, topic: String, ackDeadlineSeconds: Int): F[Subscription]
  def deleteSubscription(projectId: String, subscriptionId: String): F[Unit]
}

object PSSubApi {
  def createSubscription(
      project: String,
      subscription: String,
      topic: String,
      ackDeadlineSeconds: Int = 10
  ): ZIO[PSSubEnv, Throwable, Subscription] =
    ZIO.environmentWithZIO(_.get.createSubscription(project, subscription, topic, ackDeadlineSeconds))
  def deleteSubscription(projectId: String, subscriptionId: String): ZIO[PSSubEnv, Throwable, Unit] =
    ZIO.environmentWithZIO(_.get.deleteSubscription(projectId, subscriptionId))
}
