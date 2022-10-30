package gcp4zio.dp

import com.google.cloud.dataproc.v1.{ClusterControllerClient, ClusterControllerSettings}
import zio.{RIO, Scope, ZIO}

@SuppressWarnings(Array("org.wartremover.warts.Throw"))
object DPClusterClient {

  /** Returns AutoCloseable ClusterControllerClient object wrapped in ZIO
    * @param endpoint
    *   Dataproc cluster endpoint
    * @return
    *   RIO[Scope, ClusterControllerClient]
    */
  def apply(endpoint: String): RIO[Scope, ClusterControllerClient] = ZIO.fromAutoCloseable(ZIO.attempt {
    sys.env.getOrElse("GOOGLE_APPLICATION_CREDENTIALS", "NOT_SET_IN_ENV") match {
      case "NOT_SET_IN_ENV" => throw new RuntimeException("Set environment variable GOOGLE_APPLICATION_CREDENTIALS")
      case _ =>
        ClusterControllerClient.create(ClusterControllerSettings.newBuilder().setEndpoint(endpoint).build())
    }
  })
}
