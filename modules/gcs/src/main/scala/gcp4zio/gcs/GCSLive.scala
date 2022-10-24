package gcp4zio
package gcs

import com.google.cloud.storage.Storage.{BlobListOption, BlobTargetOption, BlobWriteOption}
import com.google.cloud.storage.{Blob, BlobId, BlobInfo, Notification, NotificationInfo, Storage}
import zio._
import zio.stream._
import java.io.{IOException, InputStream, OutputStream}
import java.nio.channels.Channels
import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters._

@SuppressWarnings(Array("org.wartremover.warts.ToString"))
case class GCSLive(client: Storage) extends GCSApi {

  override def listObjects(
      bucket: String,
      prefix: Option[String],
      recursive: Boolean,
      options: List[BlobListOption]
  ): Stream[Throwable, Blob] = {
    val inputOptions    = prefix.map(BlobListOption.prefix).toList ::: options
    val blobListOptions = if (recursive) inputOptions else BlobListOption.currentDirectory() :: inputOptions
    ZStream.fromJavaIterator(client.list(bucket, blobListOptions: _*).iterateAll().iterator())
  }

  override def lookupObject(bucket: String, prefix: String): Task[Boolean] = ZIO
    .attempt {
      val blobId = BlobId.of(bucket, prefix)
      val blob   = client.get(blobId)
      blob.exists()
    }
    .catchAll(_ => ZIO.succeed(false))

  override def deleteObject(bucket: String, prefix: String): Task[Boolean] = ZIO
    .attempt {
      val blobId = BlobId.of(bucket, prefix)
      logger.info(s"Deleting blob $blobId")
      client.delete(blobId)
    }
    .catchAll(_ => ZIO.succeed(false))

  override def putObject(bucket: String, prefix: String, file: Path, options: List[BlobTargetOption]): Task[Blob] = ZIO.attempt {
    val blobId   = BlobId.of(bucket, prefix)
    val blobInfo = BlobInfo.newBuilder(blobId).build
    logger.info(s"Copying object from local fs $file to gs://$bucket/$prefix")
    client.create(blobInfo, Files.readAllBytes(file), options: _*)
  }

  override def putObject(bucket: String, prefix: String, options: List[BlobWriteOption]): GCSSink = {
    val os: ZIO[Scope, IOException, OutputStream] = ZIO
      .fromAutoCloseable {
        ZIO.attempt {
          val blobId   = BlobId.of(bucket, prefix)
          val blobInfo = BlobInfo.newBuilder(blobId).build
          Channels.newOutputStream(client.writer(blobInfo, options: _*))
        }
      }
      .refineOrDie { case e: IOException => e }
    ZSink.fromOutputStreamScoped(os)
  }

  override def getObject(bucket: String, prefix: String, file: Path): Task[Unit] = ZIO.attempt {
    val blobId = BlobId.of(bucket, prefix)
    val blob   = client.get(blobId)
    logger.info(s"Copying object from gs://$bucket/$prefix to local fs $file")
    blob.downloadTo(file)
  }

  override def getObject(bucket: String, prefix: String, chunkSize: Int): GCSStream = {
    val is: ZIO[Scope, IOException, InputStream] = ZIO
      .fromAutoCloseable {
        ZIO.attempt {
          val blobId = BlobId.of(bucket, prefix)
          val blob   = client.get(blobId)
          Channels.newInputStream {
            val reader = blob.reader()
            reader.setChunkSize(chunkSize)
            reader
          }
        }
      }
      .refineOrDie { case e: IOException => e }
    ZStream.fromInputStreamScoped(is, chunkSize)
  }

  private def getTargetPath(srcPath: String, targetPath: String, currentPath: String): String =
    if (currentPath == srcPath) targetPath
    else (targetPath + "/" + currentPath.replace(srcPath, "")).replaceAll("//+", "/")

  override def copyObjectsGCStoGCS(
      srcBucket: String,
      srcPrefix: Option[String],
      srcRecursive: Boolean,
      srcOptions: List[BlobListOption],
      targetBucket: String,
      targetPrefix: Option[String],
      parallelism: Int
  ): Task[Unit] = listObjects(srcBucket, srcPrefix, srcRecursive, srcOptions)
    .mapZIOPar(parallelism) { blob =>
      ZIO.attempt {
        targetPrefix.fold {
          logger.info(s"Copying object from gs://$srcBucket/${blob.getName} to gs://$targetBucket/${blob.getName}")
          blob.copyTo(targetBucket)
        } { tp =>
          val targetPath = getTargetPath(srcPrefix.getOrElse(""), tp, blob.getName)
          logger.info(s"Copying object from gs://$srcBucket/${blob.getName} to gs://$targetBucket/$targetPath")
          blob.copyTo(targetBucket, targetPath)
        }
      }
    }
    .runDrain

  override def copyObjectsLOCALtoGCS(
      srcPath: String,
      targetBucket: String,
      targetPrefix: String,
      parallelism: Int,
      overwrite: Boolean
  ): Task[Unit] = {
    val opts = if (overwrite) List.empty else List(BlobTargetOption.doesNotExist())
    GCSLive
      .listLocalFsObjects(srcPath)
      .mapZIOPar(parallelism) { path =>
        val targetPath = getTargetPath(srcPath, targetPrefix, path.toString)
        putObject(targetBucket, targetPath, path, opts)
      }
      .runDrain
  }

  override def getPSNotification(bucket: String, notificationId: String): Task[Notification] = ZIO.attempt {
    client.getNotification(bucket, notificationId)
  }

  override def createPSNotification(
      bucket: String,
      topic: String,
      customAttributes: Map[String, String],
      eventType: Option[NotificationInfo.EventType],
      objectNamePrefix: Option[String],
      payloadFormat: NotificationInfo.PayloadFormat
  ): Task[Notification] = ZIO.attempt {
    val prefix = objectNamePrefix.getOrElse("")
    val notificationInfo =
      eventType.fold {
        NotificationInfo
          .newBuilder(topic)
          .setCustomAttributes(customAttributes.asJava)
          .setEventTypes()
          .setObjectNamePrefix(prefix)
          .setPayloadFormat(payloadFormat)
          .build
      } { event =>
        NotificationInfo
          .newBuilder(topic)
          .setCustomAttributes(customAttributes.asJava)
          .setEventTypes(event)
          .setObjectNamePrefix(prefix)
          .setPayloadFormat(payloadFormat)
          .build
      }

    logger.info(s"Creating Notification Configuration for gs://$bucket/$prefix")
    client.createNotification(bucket, notificationInfo)
  }

  override def deletePSNotification(bucket: String, notificationId: String): Task[Boolean] = ZIO.attempt {
    logger.info(s"Deleting Notification Configuration, ID : $notificationId")
    client.deleteNotification(bucket, notificationId)
  }

  override def listPSNotification(bucket: String): Task[List[Notification]] = ZIO.attempt {
    client.listNotifications(bucket).asScala.toList
  }
}

object GCSLive {

  def listLocalFsObjects(path: String): Stream[Throwable, Path] = ZStream
    .fromJavaIterator(Files.walk(Paths.get(path)).iterator())
    .filter(Files.isRegularFile(_))

  def apply(path: Option[String] = None): Layer[Throwable, GCSEnv] =
    ZLayer.fromZIO(ZIO.attempt(GCSClient(path)).map(client => GCSLive(client)))
}
