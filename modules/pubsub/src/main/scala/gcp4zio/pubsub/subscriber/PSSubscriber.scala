package gcp4zio.pubsub.subscriber

import com.google.api.core.ApiService
import com.google.api.gax.core.NoCredentialsProvider
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.cloud.pubsub.v1.Subscriber
import com.google.common.util.concurrent.MoreExecutors
import io.grpc.ManagedChannelBuilder
import zio.stream.{Stream, ZStream}
import zio.{Chunk, RIO, Scope, Task, ZIO}
import java.util
import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue, TimeUnit}

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
object PSSubscriber {
  private class PubsubErrorListener[R](queue: BlockingQueue[Either[InternalPubSubError, R]]) extends ApiService.Listener {
    override def failed(from: ApiService.State, failure: Throwable): Unit =
      // Puts error in queue to stop/fail the stream which will consumes from this queue
      queue.put(Left(InternalPubSubError(failure)))
  }

  private def takeNextElements[A](messages: BlockingQueue[A]): Task[Chunk[A]] =
    for {
      nextOpt <- ZIO.attempt(messages.poll()) // `poll` is non-blocking, returning `null` if queue is empty
      next <-
        if (nextOpt == null) ZIO.attempt(messages.take()) // `take` can wait for an element i.e. it is blocking
        else ZIO.succeed(nextOpt)
      chunk <- ZIO.attempt {
        val elements = new util.ArrayList[A]
        elements.add(next)
        messages.drainTo(elements)

        Chunk.fromJavaIterable(elements)
      }
    } yield chunk

  private def startAsyncPullSubscriber(
      subscriber: Subscriber,
      queue: BlockingQueue[Either[InternalPubSubError, Record]],
      config: Config
  ): RIO[Scope, Unit] = ZIO.acquireRelease(
    ZIO
      .attempt {
        subscriber.addListener(new PubsubErrorListener(queue), MoreExecutors.directExecutor)
        subscriber.startAsync().awaitRunning() // starts the subscriber and waits for it to reach the running state
      }
      .tapBoth(
        e =>
          ZIO.logError(
            s"Exception occurred while starting subscriber ${e.getMessage}. See stacktrace below for more details"
          ) *> ZIO.logError(s"${e.printStackTrace()}"),
        _ => ZIO.logInfo(s"Listening for messages on ${subscriber.getSubscriptionNameString}")
      )
  ) { _ =>
    ZIO
      .attempt(subscriber.stopAsync().awaitTerminated(config.awaitTerminatePeriod.toSeconds, TimeUnit.SECONDS))
      .tapBoth(
        config.onFailedTerminate,
        _ => ZIO.logInfo(s"Terminated subscriber for ${subscriber.getSubscriptionNameString}")
      )
      .ignore
  }

  def subscribe(project: String, subscription: String, config: Config = Config()): Stream[Throwable, Record] = {
    val setup = for {
      queue      <- ZIO.attempt(new LinkedBlockingQueue[Either[InternalPubSubError, Record]](config.maxQueueSize))
      subscriber <- PSSubscriberClient(project, subscription, config, queue)
      _          <- startAsyncPullSubscriber(subscriber, queue, config)
    } yield queue

    for {
      queue <- ZStream.scoped(setup)
      taken <- ZStream.repeatZIOChunk(takeNextElements(queue))
      msg   <- ZStream.fromZIO(ZIO.fromEither(taken))
    } yield msg
  }

  def subscribeTest(project: String, subscription: String, config: Config = Config()): Stream[Throwable, Record] = {
    val hostport            = System.getenv("PUBSUB_EMULATOR_HOST")
    val channel             = ManagedChannelBuilder.forTarget(hostport).usePlaintext().build()
    val channelProvider     = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))
    val credentialsProvider = NoCredentialsProvider.create
    val testConfig: Subscriber.Builder => Subscriber.Builder = builder =>
      builder
        .setChannelProvider(channelProvider)
        .setCredentialsProvider(credentialsProvider)
    subscribe(project, subscription, config.copy(customizeSubscriber = Some(testConfig)))
  }
}
