import gcp4zio.pubsub.publisher.{MessageEncoder, PSPublisher}
import gcp4zio.pubsub.subscriber.PSSubscriber
import gcp4zio.pubsub.subscription.PSSubscription
import gcp4zio.pubsub.topic.PSTopic
import zio._
import zio.logging.backend.SLF4J
import java.nio.charset.Charset

@SuppressWarnings(Array("org.wartremover.warts.ToString"))
object PS extends ZIOAppDefault {

  override val bootstrap = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  lazy val gcsProject: String   = sys.env("GCS_PROJECT")
  lazy val subscription: String = sys.env("SUBSCRIPTION")
  lazy val topic: String        = sys.env("TOPIC")

  private val createTopic = PSTopic.createTopic(gcsProject, topic)

  private val deleteTopic = PSTopic.deleteTopic(gcsProject, topic)

  private val createSubscription = PSSubscription.createPullSubscription(gcsProject, subscription, topic)

  private val deleteSubscription = PSSubscription.deleteSubscription(gcsProject, subscription)

  implicit val encoder: MessageEncoder[String] = (a: String) => Right(a.getBytes(Charset.defaultCharset()))

  private val produceMessages: RIO[PSPublisher, Unit] = Random.nextInt
    .flatMap(ri => PSPublisher.produce[String](s"Test Message $ri"))
    .tap(msgId => ZIO.logInfo(s"Message ID $msgId published"))
    .repeat(Schedule.spaced(5.seconds) && Schedule.forever)
    .unit

  private val consumeMessages: Task[Unit] = PSSubscriber
    .subscribeTest(gcsProject, subscription)
    .mapZIO { msg =>
      ZIO.logInfo(s"Message ID ${msg.value.getMessageId} received") *> ZIO.logInfo(msg.value.toString) *> msg.ack
    }
    .take(5)
    .runDrain

  private val setup: RIO[PSSubscription with PSTopic, Unit] = for {
    _ <- createTopic.tap(t => ZIO.logInfo(s"Created Topic ${t.toString}"))
    _ <- createSubscription.tap(s => ZIO.logInfo(s"Created Subscription ${s.toString}"))
  } yield ()

  private val flow: RIO[PSPublisher, Unit] = for {
    _ <- ZIO.logInfo("Starting Publisher") *> produceMessages.fork
    _ <- ZIO.logInfo("Starting Subscriber") *> consumeMessages
  } yield ()

  private val cleanup: RIO[PSTopic with PSSubscription, Unit] = for {
    _ <- deleteSubscription.zipLeft(ZIO.logInfo(s"Deleted Subscription"))
    _ <- deleteTopic.zipLeft(ZIO.logInfo(s"Deleted Topic"))
  } yield ()

  override def run: Task[Unit] = (setup *> flow)
    .ensuring(cleanup.ignore)
    .provide(
      PSTopic.test,
      PSSubscription.test,
      PSPublisher.test(gcsProject, topic)
    )
}
