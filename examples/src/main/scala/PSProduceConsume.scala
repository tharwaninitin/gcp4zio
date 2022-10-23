import gcp4zio.pubsub.publisher.{MessageEncoder, PSPublisher}
import gcp4zio.pubsub.subscriber.PSSubscriber
import gcp4zio.pubsub.{PSSub, PSSubApi, PSTopic, PSTopicApi}
import zio._
import zio.logging.backend.SLF4J
import java.nio.charset.Charset

@SuppressWarnings(Array("org.wartremover.warts.ToString"))
object PSProduceConsume extends ZIOAppDefault {

  override val bootstrap = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  lazy val gcsProject: String   = sys.env("GCS_PROJECT")
  lazy val subscription: String = sys.env("SUBSCRIPTION")
  lazy val topic: String        = sys.env("TOPIC")

  private val createTopic = PSTopicApi.createTopic(gcsProject, topic).provide(PSTopic.test)

  private val createSubscription = PSSubApi.createPullSubscription(gcsProject, subscription, topic).provide(PSSub.test)

  implicit val encoder: MessageEncoder[String] = (a: String) => Right(a.getBytes(Charset.defaultCharset()))

  private val produce = Random.nextInt
    .flatMap(ri => PSPublisher.produce(s"Test Message $ri"))
    .repeat(Schedule.spaced(5.seconds) && Schedule.forever)
    .provide(PSPublisher.test(gcsProject, topic))
    .fork

  private val consume = PSSubscriber.subscribe
    .mapZIO { msg =>
      ZIO.logInfo(msg.value.toString) *> msg.ack
    }
    .runDrain
    .provideSomeLayer[Scope](PSSubscriber.test(gcsProject, subscription))

  override def run: ZIO[Scope, Throwable, Unit] = createTopic *> createSubscription *> produce *> consume
}
