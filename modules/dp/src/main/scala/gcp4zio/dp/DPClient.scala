package gcp4zio.dp

import com.google.cloud.dataproc.v1.{ClusterControllerClient, ClusterControllerSettings}
import zio.{Task, ZIO}

@SuppressWarnings(Array("org.wartremover.warts.Throw"))
object DPClient {
  def apply(endpoint: String): Task[ClusterControllerClient] = ZIO.attempt {
    sys.env.getOrElse("GOOGLE_APPLICATION_CREDENTIALS", "NOT_SET_IN_ENV") match {
      case "NOT_SET_IN_ENV" => throw new RuntimeException("Set environment variable GOOGLE_APPLICATION_CREDENTIALS")
      case _ =>
        ClusterControllerClient.create(ClusterControllerSettings.newBuilder().setEndpoint(endpoint).build())
    }
  }
}
