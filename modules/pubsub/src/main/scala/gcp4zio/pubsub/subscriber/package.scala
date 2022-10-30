package gcp4zio.pubsub

import com.google.cloud.pubsub.v1.Subscriber
import com.google.pubsub.v1.PubsubMessage
import zio.{Task, ZIO}
import scala.concurrent.duration._

package object subscriber {

  case class Record(value: PubsubMessage, ack: Task[Unit], nack: Task[Unit])

  case class InternalPubSubError(cause: Throwable) extends Throwable("Java PubSub subscriber failed", cause)

  case class Config(
      maxQueueSize: Int = 1000,
      parallelPullCount: Int = 1,
      maxAckExtensionPeriod: FiniteDuration = 10.seconds,
      awaitTerminatePeriod: FiniteDuration = 20.seconds,
      customizeSubscriber: Option[Subscriber.Builder => Subscriber.Builder] = None, // modify subscriber
      onFailedTerminate: Throwable => Task[Unit] = e => ZIO.logError(s"Exception while shutting down Subscriber: ${e.getMessage}")
  )

}
