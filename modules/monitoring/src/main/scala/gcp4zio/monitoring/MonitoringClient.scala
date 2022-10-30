package gcp4zio
package monitoring

import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.{GoogleCredentials, ServiceAccountCredentials}
import com.google.cloud.monitoring.v3.{MetricServiceClient, MetricServiceSettings}
import zio.{RIO, Scope, ZIO}
import java.io.FileInputStream

object MonitoringClient {

  private def getMetricServiceClient(path: String): MetricServiceClient = {
    val credentials: GoogleCredentials = ServiceAccountCredentials.fromStream(new FileInputStream(path))
    val metricServiceSettings = MetricServiceSettings.newBuilder
      .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
      .build
    MetricServiceClient.create(metricServiceSettings)
  }

  /** Returns AutoCloseable MetricServiceClient object wrapped in ZIO
    * @param path
    *   Optional path to Service Account Credentials file
    * @return
    *   RIO[Scope, MetricServiceClient]
    */
  def apply(path: Option[String]): RIO[Scope, MetricServiceClient] = ZIO.fromAutoCloseable(ZIO.attempt {
    val envPath: String = sys.env.getOrElse("GOOGLE_APPLICATION_CREDENTIALS", "NOT_SET_IN_ENV")

    path match {
      case Some(p) =>
        logger.info("Using GCP credentials from values passed in function")
        getMetricServiceClient(p)
      case None =>
        if (envPath == "NOT_SET_IN_ENV") {
          logger.info("Using GCP credentials from local sdk")
          MetricServiceClient.create
        } else {
          logger.info("Using GCP credentials from environment variable GOOGLE_APPLICATION_CREDENTIALS")
          getMetricServiceClient(envPath)
        }
    }
  })
}
