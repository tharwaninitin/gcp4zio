package gcp4zio.pubsub.subscriber

import zio.stream.ZStream
import zio.{Chunk, Scope, Task, ZIO}
import java.util
import java.util.concurrent.BlockingQueue

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
case class PSSubscriberImpl(queue: BlockingQueue[Either[InternalPubSubError, Record]]) extends PSSubscriber {

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

  val subscribe: ZStream[Scope, Throwable, Record] =
    for {
      taken <- ZStream.repeatZIOChunk(takeNextElements(queue))
      msg   <- ZStream.fromZIO(ZIO.fromEither(taken))
    } yield msg
}
