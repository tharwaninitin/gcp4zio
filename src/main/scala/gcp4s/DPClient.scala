package gcp4s

import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.{GoogleCredentials, ServiceAccountCredentials}
import com.google.cloud.dataproc.v1.{ClusterControllerClient, ClusterControllerSettings}
import java.io.FileInputStream

object DPClient {

  private def getDP(path: String, endpoint: String): ClusterControllerClient = {
    val credentials: GoogleCredentials = ServiceAccountCredentials.fromStream(new FileInputStream(path))
    val ccs = ClusterControllerSettings.newBuilder
      .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
      .setEndpoint(endpoint)
      .build
    ClusterControllerClient.create(ccs)
  }

  def apply(endpoint: String, path: Option[String]): ClusterControllerClient = {
    val env_path: String = sys.env.getOrElse("GOOGLE_APPLICATION_CREDENTIALS", "NOT_SET_IN_ENV")

    path match {
      case Some(p) =>
        logger.info("Using GCP credentials from values passed in function")
        getDP(p, endpoint)
      case None =>
        if (env_path == "NOT_SET_IN_ENV") {
          logger.info("Using GCP credentials from local sdk")
          val ccs = ClusterControllerSettings.newBuilder.setEndpoint(endpoint).build
          ClusterControllerClient.create(ccs)
        } else {
          logger.info("Using GCP credentials from environment variable GOOGLE_APPLICATION_CREDENTIALS")
          getDP(env_path, endpoint)
        }
    }
  }
}
