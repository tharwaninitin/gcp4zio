package gcp4zio

import com.google.cloud.dataproc.v1.{JobControllerClient, JobControllerSettings}
import zio.Task

object DPJobClient {
  def apply(endpoint: String): Task[JobControllerClient] = Task {
    sys.env.getOrElse("GOOGLE_APPLICATION_CREDENTIALS", "NOT_SET_IN_ENV") match {
      case "NOT_SET_IN_ENV" => throw new RuntimeException("Set environment variable GOOGLE_APPLICATION_CREDENTIALS")
      case _ =>
        JobControllerClient.create(JobControllerSettings.newBuilder().setEndpoint(endpoint).build())
    }
  }
}
