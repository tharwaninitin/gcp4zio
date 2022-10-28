package gcp4zio.pubsub.subscription

import com.google.api.gax.core.{FixedCredentialsProvider, NoCredentialsProvider}
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.auth.oauth2.{GoogleCredentials, ServiceAccountCredentials}
import com.google.cloud.pubsub.v1.{SubscriptionAdminClient, SubscriptionAdminSettings}
import gcp4zio.pubsub.logger
import io.grpc.ManagedChannelBuilder
import zio.{RIO, Scope, ZIO}
import java.io.FileInputStream

object PSSubscriptionClient {

  private def getSubscriptionClient(path: String): SubscriptionAdminClient = {
    val credentials: GoogleCredentials = ServiceAccountCredentials.fromStream(new FileInputStream(path))
    val subscriptionAdminSettings = SubscriptionAdminSettings
      .newBuilder()
      .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
      .build
    SubscriptionAdminClient.create(subscriptionAdminSettings)
  }

  val testClient: RIO[Scope, SubscriptionAdminClient] = ZIO.fromAutoCloseable(ZIO.attempt {
    val hostport            = System.getenv("PUBSUB_EMULATOR_HOST")
    val channel             = ManagedChannelBuilder.forTarget(hostport).usePlaintext().build()
    val channelProvider     = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))
    val credentialsProvider = NoCredentialsProvider.create
    val subscriptionAdminSettings = SubscriptionAdminSettings.newBuilder
      .setTransportChannelProvider(channelProvider)
      .setCredentialsProvider(credentialsProvider)
      .build
    SubscriptionAdminClient.create(subscriptionAdminSettings)
  })

  /** Returns AutoCloseable SubscriptionAdminClient wrapped in ZIO
    * @param path
    *   Optional path to Service Account Credentials file
    * @return
    *   RIO[Scope, SubscriptionAdminClient]
    */
  def apply(path: Option[String]): RIO[Scope, SubscriptionAdminClient] = ZIO.fromAutoCloseable(ZIO.attempt {
    val envPath: String = sys.env.getOrElse("GOOGLE_APPLICATION_CREDENTIALS", "NOT_SET_IN_ENV")

    path match {
      case Some(p) =>
        logger.info("Using GCP credentials from values passed in function")
        getSubscriptionClient(p)
      case None =>
        if (envPath == "NOT_SET_IN_ENV") {
          logger.info("Using GCP credentials from local sdk")
          SubscriptionAdminClient.create()
        } else {
          logger.info("Using GCP credentials from environment variable GOOGLE_APPLICATION_CREDENTIALS")
          getSubscriptionClient(envPath)
        }
    }
  })
}
