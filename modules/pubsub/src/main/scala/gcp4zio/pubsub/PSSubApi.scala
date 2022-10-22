package gcp4zio
package pubsub

import com.google.pubsub.v1.Subscription
import zio._

trait PSSubApi[F[_]] {

  /** @param project
    *   GCP Project ID
    * @param subscription
    *   The name of the subscription
    * @param topic
    *   The name of the topic from which this subscription is receiving messages
    * @param ackDeadlineSeconds
    *   Messages not successfully acknowledged within seconds defined by this param will get resent by the server.
    * @return
    *   Subscription
    */
  def createPullSubscription(project: String, subscription: String, topic: String, ackDeadlineSeconds: Int): F[Subscription]

  /** @param project
    *   GCP Project ID
    * @param subscription
    *   The name of the subscription
    * @param topic
    *   The name of the topic from which this subscription is receiving messages
    * @param ackDeadlineSeconds
    *   Messages not successfully acknowledged within seconds defined by this param will get resent by the server.
    * @param pushEndpoint
    *   A URL locating the endpoint to which messages should be pushed.
    * @return
    *   Subscription
    */
  def createPushSubscription(
      project: String,
      subscription: String,
      topic: String,
      ackDeadlineSeconds: Int,
      pushEndpoint: String
  ): F[Subscription]

  /** @param project
    *   GCP Project ID
    * @param subscription
    *   The name of the subscription
    * @param topic
    *   The name of the topic from which this subscription is receiving messages
    * @param bqTableId
    *   The name of the table to which to write data, of the form {projectId}:{datasetId}.{tableId}
    * @return
    *   Subscription
    */
  def createBQSubscription(
      project: String,
      subscription: String,
      topic: String,
      bqTableId: String
  ): F[Subscription]

  /** @param project
    *   GCP Project ID
    * @param subscription
    *   The name of the subscription to be deleted
    * @return
    *   Unit
    */
  def deleteSubscription(project: String, subscription: String): F[Unit]
}

object PSSubApi {

  /** @param project
    *   GCP Project ID
    * @param subscription
    *   The name of the subscription
    * @param topic
    *   The name of the topic from which this subscription is receiving messages
    * @param ackDeadlineSeconds
    *   Messages not successfully acknowledged within seconds defined by this param will get resent by the server.
    * @return
    *   Subscription
    */
  def createPullSubscription(
      project: String,
      subscription: String,
      topic: String,
      ackDeadlineSeconds: Int = 10
  ): ZIO[PSSubEnv, Throwable, Subscription] =
    ZIO.environmentWithZIO(_.get.createPullSubscription(project, subscription, topic, ackDeadlineSeconds))

  /** @param project
    *   GCP Project ID
    * @param subscription
    *   The name of the subscription
    * @param topic
    *   The name of the topic from which this subscription is receiving messages
    * @param ackDeadlineSeconds
    *   Messages not successfully acknowledged within seconds defined by this param will get resent by the server.
    * @param pushEndpoint
    *   A URL locating the endpoint to which messages should be pushed.
    * @return
    *   Subscription
    */
  def createPushSubscription(
      project: String,
      subscription: String,
      topic: String,
      ackDeadlineSeconds: Int = 10,
      pushEndpoint: String
  ): ZIO[PSSubEnv, Throwable, Subscription] =
    ZIO.environmentWithZIO(_.get.createPushSubscription(project, subscription, topic, ackDeadlineSeconds, pushEndpoint))

  /** @param project
    *   GCP Project ID
    * @param subscription
    *   The name of the subscription
    * @param topic
    *   The name of the topic from which this subscription is receiving messages
    * @param bqTableId
    *   The name of the table to which to write data, of the form {projectId}:{datasetId}.{tableId}
    * @return
    *   Subscription
    */
  def createBQSubscription(
      project: String,
      subscription: String,
      topic: String,
      bqTableId: String
  ): ZIO[PSSubEnv, Throwable, Subscription] =
    ZIO.environmentWithZIO(_.get.createBQSubscription(project, subscription, topic, bqTableId))

  /** @param project
    *   GCP Project ID
    * @param subscription
    *   The name of the subscription to be deleted
    * @return
    *   Unit
    */
  def deleteSubscription(project: String, subscription: String): ZIO[PSSubEnv, Throwable, Unit] =
    ZIO.environmentWithZIO(_.get.deleteSubscription(project, subscription))
}
