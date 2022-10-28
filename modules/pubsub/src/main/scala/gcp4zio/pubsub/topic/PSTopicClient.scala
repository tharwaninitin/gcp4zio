package gcp4zio.pubsub.topic

import com.google.api.gax.core.{FixedCredentialsProvider, NoCredentialsProvider}
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.auth.oauth2.{GoogleCredentials, ServiceAccountCredentials}
import com.google.cloud.pubsub.v1.{TopicAdminClient, TopicAdminSettings}
import gcp4zio.pubsub.logger
import io.grpc.ManagedChannelBuilder
import zio.{RIO, Scope, Task, ZIO}
import java.io.FileInputStream

object PSTopicClient {

  private def getTopicClient(path: String): TopicAdminClient = {
    val credentials: GoogleCredentials = ServiceAccountCredentials.fromStream(new FileInputStream(path))
    val topicAdminSettings = TopicAdminSettings.newBuilder
      .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
      .build
    TopicAdminClient.create(topicAdminSettings)
  }

  val testClient: Task[TopicAdminClient] = ZIO.attempt {
    val hostport            = System.getenv("PUBSUB_EMULATOR_HOST")
    val channel             = ManagedChannelBuilder.forTarget(hostport).usePlaintext().build()
    val channelProvider     = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))
    val credentialsProvider = NoCredentialsProvider.create
    val topicAdminSettings = TopicAdminSettings.newBuilder
      .setTransportChannelProvider(channelProvider)
      .setCredentialsProvider(credentialsProvider)
      .build
    TopicAdminClient.create(topicAdminSettings)
  }

  /** Returns AutoCloseable TopicAdminClient wrapped in ZIO
    * @param path
    *   Optional path to Service Account Credentials file
    * @return
    *   RIO[Scope, TopicAdminClient]
    */
  def apply(path: Option[String]): RIO[Scope, TopicAdminClient] = ZIO.fromAutoCloseable(ZIO.attempt {
    val envPath: String = sys.env.getOrElse("GOOGLE_APPLICATION_CREDENTIALS", "NOT_SET_IN_ENV")

    path match {
      case Some(p) =>
        logger.info("Using GCP credentials from values passed in function")
        getTopicClient(p)
      case None =>
        if (envPath == "NOT_SET_IN_ENV") {
          logger.info("Using GCP credentials from local sdk")
          TopicAdminClient.create()
        } else {
          logger.info("Using GCP credentials from environment variable GOOGLE_APPLICATION_CREDENTIALS")
          getTopicClient(envPath)
        }
    }
  })
}
