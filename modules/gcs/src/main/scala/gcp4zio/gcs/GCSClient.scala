package gcp4zio.gcs

import com.google.auth.oauth2.{GoogleCredentials, ServiceAccountCredentials}
import com.google.cloud.storage.{Storage, StorageOptions}
import zio.{RIO, Scope, ZIO}
import java.io.FileInputStream

object GCSClient {

  private def getStorage(path: String): Storage = {
    val credentials: GoogleCredentials = ServiceAccountCredentials.fromStream(new FileInputStream(path))
    StorageOptions.newBuilder().setCredentials(credentials).build().getService
  }

  /** Returns AutoCloseable Storage object wrapped in ZIO
    * @param path
    *   Optional path to Service Account Credentials file
    * @return
    *   RIO[Scope, Storage]
    */
  def apply(path: Option[String]): RIO[Scope, Storage] = ZIO.fromAutoCloseable(ZIO.attempt {
    val envPath: String = sys.env.getOrElse("GOOGLE_APPLICATION_CREDENTIALS", "NOT_SET_IN_ENV")

    path match {
      case Some(p) =>
        logger.info("Using GCP credentials from values passed in function")
        getStorage(p)
      case None =>
        if (envPath == "NOT_SET_IN_ENV") {
          logger.info("Using GCP credentials from local sdk")
          StorageOptions.newBuilder().build().getService
        } else {
          logger.info("Using GCP credentials from environment variable GOOGLE_APPLICATION_CREDENTIALS")
          getStorage(envPath)
        }
    }
  })
}
