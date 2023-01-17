package gcp4zio.dp

import com.google.cloud.dataproc.v1.{JobControllerClient, JobControllerSettings}
import zio.{RIO, Scope, ZIO}

@SuppressWarnings(Array("org.wartremover.warts.Throw"))
object DPJobClient {

  /** Returns AutoCloseable JobControllerClient object wrapped in ZIO
    * @param endpoint
    *   Dataproc cluster endpoint
    * @return
    *   RIO[Scope, JobControllerClient]
    */
  def apply(endpoint: String): RIO[Scope, JobControllerClient] = ZIO.fromAutoCloseable(ZIO.attempt {
    sys.env.get("GOOGLE_APPLICATION_CREDENTIALS") match {
      case Some(_) =>
        logger.info("Using credentials from GOOGLE_APPLICATION_CREDENTIALS for Dataproc Job Client")
        JobControllerClient.create(JobControllerSettings.newBuilder().setEndpoint(endpoint).build())
      case None => throw new RuntimeException("Set environment variable GOOGLE_APPLICATION_CREDENTIALS")
    }
  })
}
