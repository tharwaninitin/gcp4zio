package gcp4zio.pubsub.subscriber

import com.google.api.core.ApiService
import com.google.cloud.pubsub.v1.Subscriber
import com.google.common.util.concurrent.MoreExecutors
import zio.stream.ZStream
import zio.{Chunk, RIO, Scope, Task, ZIO}
import java.util
import java.util.concurrent.{BlockingQueue, TimeUnit}

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
case class PSSubscriberImpl(subscriber: Subscriber, queue: BlockingQueue[Either[InternalPubSubError, Record]], config: Config)
    extends PSSubscriber {

  private class PubsubErrorListener[R](queue: BlockingQueue[Either[InternalPubSubError, R]]) extends ApiService.Listener {
    override def failed(from: ApiService.State, failure: Throwable): Unit =
      queue.put(Left(InternalPubSubError(failure)))
  }

  private def takeNextElements[A](messages: BlockingQueue[A]): Task[Chunk[A]] =
    for {
      nextOpt <- ZIO.attempt(messages.poll()) // `poll` is non-blocking, returning `null` if queue is empty
      next <-
        if (nextOpt == null) ZIO.attempt(messages.take()).interruptible // `take` can wait for an element, hence interruptible
        else ZIO.succeed(nextOpt)
      chunk <- ZIO.attempt {
        val elements = new util.ArrayList[A]
        elements.add(next)
        messages.drainTo(elements)

        Chunk.fromJavaIterable(elements)
      }
    } yield chunk

  private val startStreamingPull: RIO[Scope, ApiService] = ZIO.acquireRelease(
    ZIO
      .attempt {
        subscriber.addListener(new PubsubErrorListener(queue), MoreExecutors.directExecutor)
        subscriber.startAsync()
      }
      .tapBoth(
        e => ZIO.logError(s"${e.toString}"),
        _ => ZIO.logInfo(s"Successfully started Subscriber for ${subscriber.getSubscriptionNameString}")
      )
  ) { service =>
    ZIO
      .attempt(service.stopAsync().awaitTerminated(config.awaitTerminatePeriod.toSeconds, TimeUnit.SECONDS))
      .tapBoth(
        config.onFailedTerminate,
        _ => ZIO.logInfo(s"Terminated Subscriber for ${subscriber.getSubscriptionNameString}")
      )
      .ignore
  }

  val subscribe: ZStream[Scope, Throwable, Record] =
    for {
      _     <- ZStream.fromZIO(startStreamingPull)
      taken <- ZStream.repeatZIOChunk(takeNextElements(queue))
      msg   <- ZStream.fromZIO(ZIO.fromEither(taken))
    } yield msg
}
