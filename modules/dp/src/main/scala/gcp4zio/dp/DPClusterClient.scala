package gcp4zio.dp

import com.google.cloud.dataproc.v1.{ClusterControllerClient, ClusterControllerSettings}
import zio.{RIO, Scope, ZIO}

// https://cloud.google.com/docs/authentication/application-default-credentials
object DPClusterClient {

  /** Returns AutoCloseable ClusterControllerClient object wrapped in ZIO, credentials are obtained using GCP ADC strategy
    * @param endpoint
    *   Dataproc cluster endpoint
    * @return
    *   RIO[Scope, ClusterControllerClient]
    */
  def apply(endpoint: String): RIO[Scope, ClusterControllerClient] = ZIO.fromAutoCloseable(ZIO.attempt {
    val settings = ClusterControllerSettings.newBuilder.setEndpoint(endpoint).build()
    logger.info(s"Credential Provider: ${settings.getCredentialsProvider}")
    ClusterControllerClient.create(settings)
  })
}
