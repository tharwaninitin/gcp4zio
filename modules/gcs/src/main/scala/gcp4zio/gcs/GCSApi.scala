package gcp4zio
package gcs

import com.google.cloud.storage.NotificationInfo.PayloadFormat
import com.google.cloud.storage.{Blob, Notification, NotificationInfo}
import com.google.cloud.storage.Storage.{BlobListOption, BlobTargetOption, BlobWriteOption}
import zio.stream._
import zio._
import java.nio.file.Path

trait GCSApi {
  def getObject(bucket: String, prefix: String, file: Path): Task[Unit]
  def getObject(bucket: String, prefix: String, chunkSize: Int): GCSStream
  def putObject(bucket: String, prefix: String, file: Path, options: List[BlobTargetOption]): Task[Blob]
  def putObject(bucket: String, prefix: String, options: List[BlobWriteOption]): GCSSink
  def lookupObject(bucket: String, prefix: String): Task[Boolean]
  def deleteObject(bucket: String, prefix: String): Task[Boolean]
  def listObjects(
      bucket: String,
      prefix: Option[String],
      recursive: Boolean,
      options: List[BlobListOption]
  ): Stream[Throwable, Blob]
  def copyObjectsGCStoGCS(
      srcBucket: String,
      srcPrefix: Option[String],
      srcRecursive: Boolean,
      srcOptions: List[BlobListOption],
      targetBucket: String,
      targetPrefix: Option[String],
      parallelism: Int
  ): Task[Unit]
  def copyObjectsLOCALtoGCS(
      srcPath: String,
      targetBucket: String,
      targetPrefix: String,
      parallelism: Int,
      overwrite: Boolean
  ): Task[Unit]
  def getPubSubNotificationConfiguration(bucket: String, notificationId: String): Task[Notification]
  def createPubSubNotificationConfiguration(bucket: String, topic: String, customAttributes: Map[String, String], eventType: Option[NotificationInfo.EventType], objectNamePrefix: Option[String], payloadFormat: NotificationInfo.PayloadFormat): Task[Notification]
  def deletePubSubNotificationConfiguration(bucket: String, prefix: String): Task[Boolean]
  def listPubSubNotificationConfiguration(bucket: String): Task[List[Notification]]
}

object GCSApi {
  def getObject(bucket: String, prefix: String, file: Path): ZIO[GCSEnv, Throwable, Unit] =
    ZIO.environmentWithZIO(_.get.getObject(bucket, prefix, file))
  def getObject(bucket: String, prefix: String, chunkSize: Int): GCSStreamWithEnv =
    ZStream.environmentWithStream(_.get.getObject(bucket, prefix, chunkSize))
  def putObject(bucket: String, prefix: String, file: Path, options: List[BlobTargetOption]): ZIO[GCSEnv, Throwable, Blob] =
    ZIO.environmentWithZIO(_.get.putObject(bucket, prefix, file, options))
  def putObject(bucket: String, prefix: String, options: List[BlobWriteOption]): GCSSinkWithEnv =
    ZSink.environmentWithSink(_.get.putObject(bucket, prefix, options))
  def lookupObject(bucket: String, prefix: String): ZIO[GCSEnv, Throwable, Boolean] =
    ZIO.environmentWithZIO(_.get.lookupObject(bucket, prefix))
  def deleteObject(bucket: String, prefix: String): ZIO[GCSEnv, Throwable, Boolean] =
    ZIO.environmentWithZIO(_.get.deleteObject(bucket, prefix))
  def listObjects(
      bucket: String,
      prefix: Option[String] = None,
      recursive: Boolean = true,
      options: List[BlobListOption] = List.empty
  ): ZStream[GCSEnv, Throwable, Blob] =
    ZStream.environmentWithStream(_.get.listObjects(bucket, prefix, recursive, options))
  def copyObjectsGCStoGCS(
      srcBucket: String,
      srcPrefix: Option[String] = None,
      srcRecursive: Boolean = true,
      srcOptions: List[BlobListOption] = List.empty,
      targetBucket: String,
      targetPrefix: Option[String] = None,
      parallelism: Int = 2
  ): ZIO[GCSEnv, Throwable, Unit] =
    ZIO.environmentWithZIO(
      _.get.copyObjectsGCStoGCS(
        srcBucket,
        srcPrefix,
        srcRecursive,
        srcOptions,
        targetBucket,
        targetPrefix,
        parallelism
      )
    )
  def copyObjectsLOCALtoGCS(
      srcPath: String,
      targetBucket: String,
      targetPrefix: String,
      parallelism: Int,
      overwrite: Boolean
  ): ZIO[GCSEnv, Throwable, Unit] =
    ZIO.environmentWithZIO(_.get.copyObjectsLOCALtoGCS(srcPath, targetBucket, targetPrefix, parallelism, overwrite))
  def getNotificationConfiguration(bucket: String, notificationId: String): ZIO[GCSEnv, Throwable, Notification] =
    ZIO.environmentWithZIO(_.get.getPubSubNotificationConfiguration(bucket, notificationId))
  def createNotificationConfiguration(
      bucket: String,
      topic: String,
      customAttributes: Map[String, String] = Map[String, String]().empty,
      eventType: Option[NotificationInfo.EventType] = None,
      objectNamePrefix: Option[String] = None,
      payloadFormat: NotificationInfo.PayloadFormat = PayloadFormat.JSON_API_V1
  ): ZIO[GCSEnv, Throwable, Notification] =
    ZIO.environmentWithZIO(_.get.createPubSubNotificationConfiguration(bucket, topic, customAttributes, eventType, objectNamePrefix, payloadFormat))
  def deleteNotificationConfiguration(bucket: String, prefix: String): ZIO[GCSEnv, Throwable, Boolean] =
    ZIO.environmentWithZIO(_.get.deletePubSubNotificationConfiguration(bucket, prefix))
  def listNotificationConfiguration(bucket: String): ZIO[GCSEnv, Throwable, List[Notification]] =
    ZIO.environmentWithZIO(_.get.listPubSubNotificationConfiguration(bucket: String))
}
