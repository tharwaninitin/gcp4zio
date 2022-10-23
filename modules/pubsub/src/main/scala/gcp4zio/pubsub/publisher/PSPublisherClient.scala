package gcp4zio.pubsub.publisher

import com.google.api.gax.batching.BatchingSettings
import com.google.cloud.pubsub.v1.Publisher
import com.google.pubsub.v1.ProjectTopicName
import org.threeten.bp.Duration
import zio.{Scope, ZIO}
import java.util.concurrent.TimeUnit

object PSPublisherClient {

  def apply(project: String, topic: String, config: Config): ZIO[Scope, Throwable, Publisher] =
    ZIO.acquireRelease(ZIO.attempt {
      val publisherBuilder = Publisher
        .newBuilder(ProjectTopicName.of(project, topic))
        .setBatchingSettings(
          BatchingSettings
            .newBuilder()
            .setElementCountThreshold(config.batchSize)
            .setRequestByteThreshold(
              config.requestByteThreshold.getOrElse[Long](config.batchSize * config.averageMessageSize * 2L)
            )
            .setDelayThreshold(Duration.ofMillis(config.delayThreshold.toMillis))
            .build()
        )

      config.customizePublisher
        .map(f => f(publisherBuilder))
        .getOrElse(publisherBuilder)
        .build()

    }) { publisher =>
      (for {
         _ <- ZIO.attempt(publisher.shutdown())
         _ <- ZIO.attempt(publisher.awaitTermination(config.awaitTerminatePeriod.toMillis, TimeUnit.MILLISECONDS))
       } yield ()).ignore
    }
}
