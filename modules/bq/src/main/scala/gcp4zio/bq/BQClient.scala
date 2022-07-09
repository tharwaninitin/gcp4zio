package gcp4zio.bq

import com.google.auth.oauth2.{GoogleCredentials, ServiceAccountCredentials}
import com.google.cloud.bigquery.{BigQuery, BigQueryOptions}
import java.io.FileInputStream

object BQClient {

  private def getBQ(path: String): BigQuery = {
    val credentials: GoogleCredentials = ServiceAccountCredentials.fromStream(new FileInputStream(path))
    BigQueryOptions.newBuilder().setCredentials(credentials).build().getService
  }

  def apply(path: Option[String] = None): BigQuery = {
    val envPath: String = sys.env.getOrElse("GOOGLE_APPLICATION_CREDENTIALS", "NOT_SET_IN_ENV")
    path match {
      case Some(p) =>
        logger.info("Using GCP credentials from values passed in function")
        getBQ(p)
      case None =>
        if (envPath == "NOT_SET_IN_ENV") {
          logger.info("Using GCP credentials from local sdk")
          BigQueryOptions.getDefaultInstance.getService
        } else {
          logger.info("Using GCP credentials from environment variable GOOGLE_APPLICATION_CREDENTIALS")
          getBQ(envPath)
        }
    }
  }
}
