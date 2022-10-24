package gcp4zio.pubsub.subscriber

import com.google.api.gax.batching.FlowControlSettings
import com.google.cloud.pubsub.v1.{AckReplyConsumer, MessageReceiver, Subscriber}
import com.google.pubsub.v1.{ProjectSubscriptionName, PubsubMessage}
import org.threeten.bp.Duration
import zio.{Task, ZIO}
import java.util.concurrent.BlockingQueue

object PSSubscriberClient {

  class PubsubMessageReceiver[E](queue: BlockingQueue[Either[E, Record]]) extends MessageReceiver {
    override def receiveMessage(message: PubsubMessage, consumer: AckReplyConsumer): Unit =
      queue.put(Right(Record(message, ZIO.attempt(consumer.ack()), ZIO.attempt(consumer.nack()))))
  }

  def apply(
      project: String,
      subscription: String,
      config: Config,
      queue: BlockingQueue[Either[InternalPubSubError, Record]]
  ): Task[Subscriber] = ZIO.attempt {
    val receiver = new PubsubMessageReceiver(queue)

    val subscriptionName = ProjectSubscriptionName.of(project, subscription)

    val subscriberBuilder = Subscriber
      .newBuilder(subscriptionName, receiver)
      .setFlowControlSettings(
        FlowControlSettings
          .newBuilder()
          .setMaxOutstandingElementCount(config.maxQueueSize.toLong)
          .build()
      )
      .setParallelPullCount(config.parallelPullCount)
      .setMaxAckExtensionPeriod(Duration.ofMillis(config.maxAckExtensionPeriod.toMillis))

    config.customizeSubscriber
      .map(f => f(subscriberBuilder))
      .getOrElse(subscriberBuilder)
      .build()
  }
}
