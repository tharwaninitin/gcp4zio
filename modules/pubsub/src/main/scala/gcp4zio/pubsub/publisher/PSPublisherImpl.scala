package gcp4zio.pubsub.publisher

import com.google.cloud.pubsub.v1.Publisher
import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import zio.{Task, ZIO}
import java.util.UUID
import scala.jdk.CollectionConverters._

@SuppressWarnings(Array("org.wartremover.warts.ToString"))
case class PSPublisherImpl(client: Publisher) extends PSPublisher {
  final override def produce[A: MessageEncoder](
      data: A,
      attributes: Map[String, String] = Map.empty,
      uniqueId: String = UUID.randomUUID.toString
  ): Task[String] =
    ZIO.fromEither(MessageEncoder[A].encode(data)).flatMap { v =>
      val message = PubsubMessage.newBuilder
        .setData(ByteString.copyFrom(v))
        .setMessageId(uniqueId)
        .putAllAttributes(attributes.asJava)
        .build()

      ZIO.fromFutureJava(client.publish(message))
    }
}
