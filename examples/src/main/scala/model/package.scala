import com.google.pubsub.v1.PubsubMessage
import zio.{Task, ZIO}
import scala.concurrent.duration.FiniteDuration
import scala.util.control.NoStackTrace
import scala.concurrent.duration._

package object model {
  case class Record(value: PubsubMessage, ack: Task[Unit], nack: Task[Unit])

  case class InternalPubSubError(cause: Throwable)
      extends Throwable("Internal Java PubSub consumer failed", cause)
      with NoStackTrace

  case class Config(
      maxQueueSize: Int = 1000,
      parallelPullCount: Int = 3,
      maxAckExtensionPeriod: FiniteDuration = 10.seconds,
      awaitTerminatePeriod: FiniteDuration = 30.seconds,
      onFailedTerminate: Throwable => Task[Unit] = e => ZIO.logError(s"Failed because of ${e.getMessage}")
  )

}
