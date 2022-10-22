import gcp4zio.pubsub._
import zio._
import zio.logging.backend.SLF4J

object PSSetup extends ZIOAppDefault {

  override val bootstrap = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  lazy val gcsProject: String    = sys.env("GCS_PROJECT")
  lazy val topic: String         = sys.env("TOPIC")
  lazy val subscription1: String = sys.env("SUBSCRIPTION_1")

  private val step1 = PSTopicApi.createTopic(gcsProject, topic)
  private val step2 = PSSubApi.createPullSubscription(gcsProject, subscription1, topic)
  private val setup = (step1 *> step2).provide(PSSub.test ++ PSTopic.test)

  override def run: ZIO[Scope, Throwable, Unit] = setup.unit
}
