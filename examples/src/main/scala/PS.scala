import gcp4zio.pubsub.publisher.{MessageEncoder, PSPublisher}
import gcp4zio.pubsub.subscriber.PSSubscriber
import gcp4zio.pubsub.{PSSub, PSSubApi, PSTopic, PSTopicApi}
import zio._
import zio.logging.backend.SLF4J
import java.nio.charset.Charset

@SuppressWarnings(Array("org.wartremover.warts.ToString"))
object PS extends ZIOAppDefault {

  override val bootstrap = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  lazy val gcsProject: String   = sys.env("GCS_PROJECT")
  lazy val subscription: String = sys.env("SUBSCRIPTION")
  lazy val topic: String        = sys.env("TOPIC")

  private val createTopic = PSTopicApi.createTopic(gcsProject, topic)

  private val deleteTopic = PSTopicApi.deleteTopic(gcsProject, topic)

  private val createSubscription = PSSubApi.createPullSubscription(gcsProject, subscription, topic)

  private val deleteSubscription = PSSubApi.deleteSubscription(gcsProject, subscription)

  implicit val encoder: MessageEncoder[String] = (a: String) => Right(a.getBytes(Charset.defaultCharset()))

  private val produce = Random.nextInt
    .flatMap(ri => PSPublisher.produce(s"Test Message $ri"))
    .tap(msgId => ZIO.logInfo(s"Message ID $msgId published"))
    .repeat(Schedule.spaced(5.seconds) && Schedule.forever)

  private val consume = PSSubscriber.subscribe
    .mapZIO { msg =>
      ZIO.logInfo(msg.value.toString) *> msg.ack
    }
    .take(10)
    .runDrain

  private val flow = for {
    _ <- createTopic.tap(t => ZIO.logInfo(s"Created Topic ${t.toString}"))
    _ <- createSubscription.tap(s => ZIO.logInfo(s"Created Subscription ${s.toString}"))
    _ <- ZIO.logInfo("Starting Publisher") *> produce.fork
    _ <- ZIO.logInfo("Starting Subscriber") *> consume
  } yield ()

  private val cleanup = for {
    _ <- deleteSubscription.zipLeft(ZIO.logInfo(s"Deleted Subscription"))
    _ <- deleteTopic.zipLeft(ZIO.logInfo(s"Deleted Topic"))
  } yield ()

  override def run: ZIO[Scope, Throwable, Unit] =
    flow
      .ensuring(cleanup.ignore)
      .provideSome[Scope](
        PSTopic.test,
        PSSub.test,
        PSPublisher.test(gcsProject, topic),
        PSSubscriber.test(gcsProject, subscription)
      )
}
