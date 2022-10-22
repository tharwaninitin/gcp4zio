package gcp4zio
package pubsub

import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.{GoogleCredentials, ServiceAccountCredentials}
import com.google.cloud.pubsub.v1.{TopicAdminClient, TopicAdminSettings}
import zio.{Task, ZIO}

import java.io.FileInputStream

object PSTopicClient {

  private def getTopicClient(path: String): TopicAdminClient = {
    val credentials: GoogleCredentials = ServiceAccountCredentials.fromStream(new FileInputStream(path))
    val topicAdminSettings =
      TopicAdminSettings.newBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build
    TopicAdminClient.create(topicAdminSettings)
  }

  def apply(path: Option[String]): Task[TopicAdminClient] = ZIO.attempt {
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
  }
}
