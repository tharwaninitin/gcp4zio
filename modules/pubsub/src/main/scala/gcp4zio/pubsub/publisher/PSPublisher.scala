package gcp4zio.pubsub.publisher

import com.google.api.gax.core.NoCredentialsProvider
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.cloud.pubsub.v1.Publisher
import io.grpc.ManagedChannelBuilder
import zio.{RIO, Task, TaskLayer, ZIO, ZLayer}
import java.util.UUID

trait PSPublisher {
  def produce[A: MessageEncoder](data: A, attributes: Map[String, String], uniqueId: String): Task[String]
}

@SuppressWarnings(Array("org.wartremover.warts.ToString"))
object PSPublisher {
  def produce[A: MessageEncoder](
      data: A,
      attributes: Map[String, String] = Map.empty,
      uniqueId: String = UUID.randomUUID.toString
  ): RIO[PSPublisher, String] = ZIO.environmentWithZIO(_.get.produce(data, attributes, uniqueId))

  def live(project: String, topic: String, config: Config = Config()): TaskLayer[PSPublisher] =
    ZLayer.scoped(PSPublisherClient(project, topic, config).map(client => PSPublisherImpl(client)))

  def test(project: String, topic: String, config: Config = Config()): TaskLayer[PSPublisher] = {
    val hostport            = System.getenv("PUBSUB_EMULATOR_HOST")
    val channel             = ManagedChannelBuilder.forTarget(hostport).usePlaintext().build()
    val channelProvider     = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))
    val credentialsProvider = NoCredentialsProvider.create
    val testConfig: Publisher.Builder => Publisher.Builder = builder =>
      builder
        .setChannelProvider(channelProvider)
        .setCredentialsProvider(credentialsProvider)
    live(project, topic, config.copy(customizePublisher = Some(testConfig)))
  }
}
