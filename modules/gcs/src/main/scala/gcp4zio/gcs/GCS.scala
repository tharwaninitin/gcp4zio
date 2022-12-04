package gcp4zio
package gcs

import com.google.cloud.storage.NotificationInfo.PayloadFormat
import com.google.cloud.storage.Storage.{BlobListOption, BlobTargetOption, BlobWriteOption}
import com.google.cloud.storage.{Blob, Notification, NotificationInfo}
import zio._
import zio.stream._
import java.nio.file.Path

trait GCS {
  def getObject(bucket: String, prefix: String, file: Path): Task[Unit]
  def getObject(bucket: String, prefix: String, chunkSize: Int): GCSStream
  def putObject(bucket: String, prefix: String, file: Path, options: List[BlobTargetOption], log: Boolean): Task[Blob]
  def putObject(bucket: String, prefix: String, options: List[BlobWriteOption]): GCSSink
  def lookupObject(bucket: String, prefix: String): Task[Boolean]
  def deleteObject(bucket: String, prefix: String): Task[Boolean]
  def listObjects(bucket: String, prefix: Option[String], recursive: Boolean, options: List[BlobListOption]): TaskStream[Blob]
  def copyObjectsGCStoGCS(
      srcBucket: String,
      srcPrefix: Option[String],
      srcRecursive: Boolean,
      srcOptions: List[BlobListOption],
      targetBucket: String,
      targetPrefix: Option[String],
      parallelism: Int,
      log: Boolean
  ): Task[Long]
  def copyObjectsLOCALtoGCS(
      srcPath: String,
      targetBucket: String,
      targetPrefix: String,
      parallelism: Int,
      overwrite: Boolean,
      log: Boolean
  ): Task[Long]
  def getPSNotification(bucket: String, notificationId: String): Task[Notification]
  def createPSNotification(
      bucket: String,
      topic: String,
      customAttributes: Map[String, String],
      eventType: Option[NotificationInfo.EventType],
      objectNamePrefix: Option[String],
      payloadFormat: NotificationInfo.PayloadFormat
  ): Task[Notification]
  def deletePSNotification(bucket: String, prefix: String): Task[Boolean]
  def listPSNotification(bucket: String): Task[List[Notification]]
}

object GCS {
  def getObject(bucket: String, prefix: String, file: Path): ZIO[GCS, Throwable, Unit] =
    ZIO.environmentWithZIO(_.get.getObject(bucket, prefix, file))
  def getObject(bucket: String, prefix: String, chunkSize: Int): GCSStreamWithEnv =
    ZStream.environmentWithStream(_.get.getObject(bucket, prefix, chunkSize))
  def putObject(
      bucket: String,
      prefix: String,
      file: Path,
      options: List[BlobTargetOption] = List.empty,
      log: Boolean = false
  ): ZIO[GCS, Throwable, Blob] =
    ZIO.environmentWithZIO(_.get.putObject(bucket, prefix, file, options, log))
  def putObject(bucket: String, prefix: String, options: List[BlobWriteOption]): GCSSinkWithEnv =
    ZSink.environmentWithSink(_.get.putObject(bucket, prefix, options))
  def lookupObject(bucket: String, prefix: String): ZIO[GCS, Throwable, Boolean] =
    ZIO.environmentWithZIO(_.get.lookupObject(bucket, prefix))
  def deleteObject(bucket: String, prefix: String): ZIO[GCS, Throwable, Boolean] =
    ZIO.environmentWithZIO(_.get.deleteObject(bucket, prefix))
  def listObjects(
      bucket: String,
      prefix: Option[String] = None,
      recursive: Boolean = true,
      options: List[BlobListOption] = List.empty
  ): ZStream[GCS, Throwable, Blob] =
    ZStream.environmentWithStream(_.get.listObjects(bucket, prefix, recursive, options))
  def copyObjectsGCStoGCS(
      srcBucket: String,
      srcPrefix: Option[String] = None,
      srcRecursive: Boolean = true,
      srcOptions: List[BlobListOption] = List.empty,
      targetBucket: String,
      targetPrefix: Option[String] = None,
      parallelism: Int = 2,
      log: Boolean = false
  ): ZIO[GCS, Throwable, Long] =
    ZIO.environmentWithZIO(
      _.get.copyObjectsGCStoGCS(
        srcBucket,
        srcPrefix,
        srcRecursive,
        srcOptions,
        targetBucket,
        targetPrefix,
        parallelism,
        log
      )
    )
  def copyObjectsLOCALtoGCS(
      srcPath: String,
      targetBucket: String,
      targetPrefix: String,
      parallelism: Int,
      overwrite: Boolean,
      log: Boolean = false
  ): ZIO[GCS, Throwable, Long] =
    ZIO.environmentWithZIO(_.get.copyObjectsLOCALtoGCS(srcPath, targetBucket, targetPrefix, parallelism, overwrite, log))
  def getPSNotification(bucket: String, notificationId: String): ZIO[GCS, Throwable, Notification] =
    ZIO.environmentWithZIO(_.get.getPSNotification(bucket, notificationId))
  def createPSNotification(
      bucket: String,
      topic: String,
      customAttributes: Map[String, String] = Map[String, String]().empty,
      eventType: Option[NotificationInfo.EventType] = None,
      objectNamePrefix: Option[String] = None,
      payloadFormat: NotificationInfo.PayloadFormat = PayloadFormat.JSON_API_V1
  ): ZIO[GCS, Throwable, Notification] =
    ZIO.environmentWithZIO(
      _.get.createPSNotification(bucket, topic, customAttributes, eventType, objectNamePrefix, payloadFormat)
    )
  def deletePSNotification(bucket: String, prefix: String): ZIO[GCS, Throwable, Boolean] =
    ZIO.environmentWithZIO(_.get.deletePSNotification(bucket, prefix))
  def listPSNotification(bucket: String): ZIO[GCS, Throwable, List[Notification]] =
    ZIO.environmentWithZIO(_.get.listPSNotification(bucket: String))
  def live(path: Option[String] = None): TaskLayer[GCS] = ZLayer.scoped(GCSClient(path).map(client => GCSImpl(client)))
  def test(url: String = "http://0.0.0.0:8080", project: String = "testproject"): TaskLayer[GCS] =
    ZLayer.scoped(GCSClient.testClient(url, project).map(client => GCSImpl(client)))
}
