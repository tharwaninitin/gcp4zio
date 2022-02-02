package gcp4s

import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.{GoogleCredentials, ServiceAccountCredentials}
import com.google.cloud.dataproc.v1.{JobControllerClient, JobControllerSettings}
import java.io.FileInputStream

object DPJobClient {

  private def getDPJob(endpoint: String, path: String): JobControllerClient = {
    val credentials: GoogleCredentials = ServiceAccountCredentials.fromStream(new FileInputStream(path))
    val jcs = JobControllerSettings
      .newBuilder()
      .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
      .setEndpoint(endpoint)
      .build()
    JobControllerClient.create(jcs)
  }

  def apply(endpoint: String, path: Option[String]): JobControllerClient = {
    val env_path: String = sys.env.getOrElse("GOOGLE_APPLICATION_CREDENTIALS", "NOT_SET_IN_ENV")

    path match {
      case Some(p) =>
        logger.info("Using GCP credentials from values passed in function")
        getDPJob(endpoint, p)
      case None =>
        if (env_path == "NOT_SET_IN_ENV") {
          logger.info("Using GCP credentials from local sdk")
          val jcs = JobControllerSettings.newBuilder().setEndpoint(endpoint).build()
          JobControllerClient.create(jcs)
        } else {
          logger.info("Using GCP credentials from environment variable GOOGLE_APPLICATION_CREDENTIALS")
          getDPJob(endpoint, env_path)
        }
    }
  }
}
