import com.google.api.core.ApiService
import com.google.api.gax.batching.FlowControlSettings
import com.google.api.gax.core.NoCredentialsProvider
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.cloud.pubsub.v1.{AckReplyConsumer, MessageReceiver, Subscriber}
import com.google.common.util.concurrent.MoreExecutors
import com.google.pubsub.v1.{ProjectSubscriptionName, PubsubMessage}
import io.grpc.ManagedChannelBuilder
import model.{Config, InternalPubSubError, Record}
import org.threeten.bp.Duration
import zio.stream._
import zio.{Chunk, Scope, Task, ZIO}
import java.util
import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue, TimeUnit}
import scala.jdk.CollectionConverters._

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
object PSSubscriber {

  private val hostport            = System.getenv("PUBSUB_EMULATOR_HOST")
  private val channel             = ManagedChannelBuilder.forTarget(hostport).usePlaintext().build()
  private val channelProvider     = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))
  private val credentialsProvider = NoCredentialsProvider.create

  def createSubscriber(
      project: String,
      subscription: String,
      config: Config,
      queue: BlockingQueue[Either[InternalPubSubError, Record]]
  ): ZIO[Scope, Throwable, ApiService] =
    ZIO.acquireRelease {
      ZIO
        .attempt {
          val receiver = new PubsubMessageReceiver(queue)

          val subscriptionName = ProjectSubscriptionName.of(project, subscription)

          val sub = Subscriber
            .newBuilder(subscriptionName, receiver)
            .setFlowControlSettings(
              FlowControlSettings
                .newBuilder()
                .setMaxOutstandingElementCount(config.maxQueueSize.toLong)
                .build()
            )
            .setChannelProvider(channelProvider)
            .setCredentialsProvider(credentialsProvider)
            .setParallelPullCount(config.parallelPullCount)
            .setMaxAckExtensionPeriod(Duration.ofMillis(config.maxAckExtensionPeriod.toMillis))
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

  class PubsubMessageReceiver[E](queue: BlockingQueue[Either[E, Record]]) extends MessageReceiver {
    override def receiveMessage(message: PubsubMessage, consumer: AckReplyConsumer): Unit =
      queue.put(Right(Record(message, ZIO.attempt(consumer.ack()), ZIO.attempt(consumer.nack()))))
  }

  class PubsubErrorListener[R](queue: BlockingQueue[Either[InternalPubSubError, R]]) extends ApiService.Listener {
    override def failed(from: ApiService.State, failure: Throwable): Unit =
      queue.put(Left(InternalPubSubError(failure)))
  }

  def takeNextElements[A](messages: BlockingQueue[A]): Task[Chunk[A]] =
    for {
      nextOpt <- ZIO.attempt(messages.poll()) // `poll` is non-blocking, returning `null` if queue is empty
      next <-
        if (nextOpt == null) ZIO.attempt(messages.take()).interruptible // `take` can wait for an element, hence interruptible
        else ZIO.succeed(nextOpt)
      chunk <- ZIO.attempt {
        val elements = new util.ArrayList[A]
        elements.add(next)
        messages.drainTo(elements)

        Chunk.fromIterable(elements.asScala)
      }
    } yield chunk

  def subscribe(
      project: String,
      subscription: String,
      config: Config
  ): ZStream[Scope, Throwable, Record] =
    for {
      queue <- ZStream
        .fromZIO(
          ZIO.attempt(new LinkedBlockingQueue[Either[InternalPubSubError, Record]](config.maxQueueSize))
        )
        .tap(_ => ZIO.logInfo("Created Queue"))
      _ <- ZStream
        .fromZIO(PSSubscriber.createSubscriber(project, subscription, config, queue))
        .tap(_ => ZIO.logInfo("Waiting for messages"))
      taken <- ZStream.repeatZIOChunk(takeNextElements(queue))
      // Only retains the first error (if there are multiple), but that is OK, the stream is failing anyway...
      msg <- ZStream.fromZIO(ZIO.fromEither(taken))
    } yield msg
}
