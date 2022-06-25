package gcp4zio
package dp

import com.google.cloud.dataproc.v1.{JobControllerClient, JobControllerSettings}
import zio.{Task, ZIO}

@SuppressWarnings(Array("org.wartremover.warts.Throw"))
object DPJobClient {
  def apply(endpoint: String): Task[JobControllerClient] = ZIO.attempt {
    sys.env.getOrElse("GOOGLE_APPLICATION_CREDENTIALS", "NOT_SET_IN_ENV") match {
      case "NOT_SET_IN_ENV" => throw new RuntimeException("Set environment variable GOOGLE_APPLICATION_CREDENTIALS")
      case _ =>
        JobControllerClient.create(JobControllerSettings.newBuilder().setEndpoint(endpoint).build())
    }
  }
}
