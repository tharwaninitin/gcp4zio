package gcp4zio.pubsub.subscriber

import com.google.api.gax.core.NoCredentialsProvider
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.cloud.pubsub.v1.Subscriber
import io.grpc.ManagedChannelBuilder
import zio.stream.ZStream
import zio.{Scope, TaskLayer, ZIO, ZLayer}
import java.util.concurrent.LinkedBlockingQueue

trait PSSubscriber {
  val subscribe: ZStream[Scope, Throwable, Record]
}

object PSSubscriber {
  val subscribe: ZStream[PSSubscriber with Scope, Throwable, Record] =
    ZStream.environmentWithStream[PSSubscriber](_.get.subscribe)

  def live(project: String, subscription: String, config: Config = Config()): TaskLayer[PSSubscriber] = {
    val program = for {
      queue <- ZIO.attempt(new LinkedBlockingQueue[Either[InternalPubSubError, Record]](config.maxQueueSize))
      _     <- PSSubscriberClient(project, subscription, config, queue)
    } yield queue
    ZLayer.scoped(program.map(queue => PSSubscriberImpl(queue)))
  }

  def test(project: String, subscription: String, config: Config = Config()): TaskLayer[PSSubscriber] = {
    val hostport            = System.getenv("PUBSUB_EMULATOR_HOST")
    val channel             = ManagedChannelBuilder.forTarget(hostport).usePlaintext().build()
    val channelProvider     = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))
    val credentialsProvider = NoCredentialsProvider.create
    val testConfig: Subscriber.Builder => Subscriber.Builder = builder =>
      builder
        .setChannelProvider(channelProvider)
        .setCredentialsProvider(credentialsProvider)
    live(project, subscription, config.copy(customizeSubscriber = Some(testConfig)))
  }
}
