import zio._
import model._
import zio.logging.backend.SLF4J

object PSSubscriberExample extends ZIOAppDefault {

  override val bootstrap = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  lazy val gcsProject: String    = sys.env("GCS_PROJECT")
  lazy val subscription1: String = sys.env("SUBSCRIPTION_1")

  private val step3 = PSSubscriber.subscribe(gcsProject, subscription1, Config())

  override def run: ZIO[Scope, Throwable, Unit] = step3.mapZIO { msg =>
    ZIO.logInfo(msg.value.toString) *> msg.ack
  }.runDrain
}
