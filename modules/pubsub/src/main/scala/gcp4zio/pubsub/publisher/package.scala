package gcp4zio.pubsub

import com.google.cloud.pubsub.v1.Publisher
import zio.{Task, ZIO}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

package object publisher {

  trait MessageEncoder[A] {
    def encode(a: A): Either[Throwable, Array[Byte]]
  }

  object MessageEncoder {
    def apply[A: MessageEncoder]: MessageEncoder[A] = implicitly
  }

  case class Config(
      batchSize: Long = 1,
      delayThreshold: FiniteDuration = 100.millis,
      requestByteThreshold: Option[Long] = None,
      averageMessageSize: Long = 1024,                                           // 1kB
      customizePublisher: Option[Publisher.Builder => Publisher.Builder] = None, // modify publisher
      awaitTerminatePeriod: FiniteDuration = 30.seconds,                         // termination
      onFailedTerminate: Throwable => Task[Unit] = e => ZIO.logError(s"Exception: ${e.getMessage}")
  )
}
