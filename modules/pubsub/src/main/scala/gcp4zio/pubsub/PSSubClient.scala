package gcp4zio
package pubsub

import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.{GoogleCredentials, ServiceAccountCredentials}
import com.google.cloud.pubsub.v1.{SubscriptionAdminClient, SubscriptionAdminSettings}
import zio.{Task, ZIO}

import java.io.FileInputStream

object PSSubClient {

  private def getSubscriptionClient(path: String): SubscriptionAdminClient = {
    val credentials: GoogleCredentials = ServiceAccountCredentials.fromStream(new FileInputStream(path))
    val subscriptionAdminSettings = SubscriptionAdminSettings
      .newBuilder()
      .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
      .build
    SubscriptionAdminClient.create(subscriptionAdminSettings)
  }

  def apply(path: Option[String]): Task[SubscriptionAdminClient] = ZIO.attempt {
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
  }
}
