package gcp4zio.pubsub.subscriber

import com.google.api.core.ApiService
import com.google.api.gax.batching.FlowControlSettings
import com.google.cloud.pubsub.v1.{AckReplyConsumer, MessageReceiver, Subscriber}
import com.google.common.util.concurrent.MoreExecutors
import com.google.pubsub.v1.{ProjectSubscriptionName, PubsubMessage}
import org.threeten.bp.Duration
import zio.{RIO, Scope, ZIO}
import java.util.concurrent.{BlockingQueue, TimeUnit}

object PSSubscriberClient {
  class PubsubMessageReceiver[E](queue: BlockingQueue[Either[E, Record]]) extends MessageReceiver {
    override def receiveMessage(message: PubsubMessage, consumer: AckReplyConsumer): Unit =
      queue.put(Right(Record(message, ZIO.attempt(consumer.ack()), ZIO.attempt(consumer.nack()))))
  }

  class PubsubErrorListener[R](queue: BlockingQueue[Either[InternalPubSubError, R]]) extends ApiService.Listener {
    override def failed(from: ApiService.State, failure: Throwable): Unit =
      queue.put(Left(InternalPubSubError(failure)))
  }

  def apply(
      project: String,
      subscription: String,
      config: Config,
      queue: BlockingQueue[Either[InternalPubSubError, Record]]
  ): RIO[Scope, ApiService] = ZIO.acquireRelease {
    ZIO
      .attempt {
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

        val sub = config.customizeSubscriber
          .map(f => f(subscriberBuilder))
          .getOrElse(subscriberBuilder)
          .build()

        sub.addListener(new PubsubErrorListener(queue), MoreExecutors.directExecutor)

        sub.startAsync()
      }
      .tapError(e => ZIO.logError(s"${e.toString}"))
  } { service =>
    ZIO
      .attempt(service.stopAsync().awaitTerminated(config.awaitTerminatePeriod.toSeconds, TimeUnit.SECONDS))
      .tapError(config.onFailedTerminate)
      .ignore
  }
}
