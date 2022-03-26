package gcp4zio

import com.google.cloud.storage.Storage.{BlobListOption, BlobTargetOption, BlobWriteOption}
import com.google.cloud.storage.{Blob, BlobId, BlobInfo, Storage}
import gcp4zio.GCS._
import zio._
import zio.stream._
import java.io.{IOException, InputStream, OutputStream}
import java.nio.channels.Channels
import java.nio.file.{Files, Path, Paths}

@SuppressWarnings(Array("org.wartremover.warts.ToString"))
case class GCS(client: Storage) extends GCSApi.Service {

  override def listObjects(
      bucket: String,
      prefix: Option[String],
      recursive: Boolean,
      options: List[BlobListOption]
  ): Stream[Throwable, Blob] = {
    val inputOptions    = prefix.map(BlobListOption.prefix).toList ::: options
    val blobListOptions = if (recursive) inputOptions else BlobListOption.currentDirectory() :: inputOptions
    Stream.fromJavaIterator(client.list(bucket, blobListOptions: _*).iterateAll().iterator())
  }

  override def lookupObject(bucket: String, prefix: String): Task[Boolean] = Task {
    val blobId = BlobId.of(bucket, prefix)
    val blob   = client.get(blobId)
    blob.exists()
  }.catchAll(_ => UIO(false))

  override def deleteObject(bucket: String, prefix: String): Task[Boolean] = Task {
    val blobId = BlobId.of(bucket, prefix)
    logger.info(s"Deleting blob $blobId")
    client.delete(blobId)
  }.catchAll(_ => UIO(false))

  override def putObject(bucket: String, prefix: String, file: Path, options: List[BlobTargetOption]): Task[Blob] = Task {
    val blobId   = BlobId.of(bucket, prefix)
    val blobInfo = BlobInfo.newBuilder(blobId).build
    logger.info(s"Copying object from local fs $file to gs://$bucket/$prefix")
    client.create(blobInfo, Files.readAllBytes(file), options: _*)
  }

  override def putObject(bucket: String, prefix: String, options: List[BlobWriteOption]): GCSSink = {
    val os: Managed[IOException, OutputStream] = Managed
      .fromAutoCloseable {
        Task {
          val blobId   = BlobId.of(bucket, prefix)
          val blobInfo = BlobInfo.newBuilder(blobId).build
          Channels.newOutputStream(client.writer(blobInfo, options: _*))
        }
      }
      .refineOrDie { case e: IOException => e }
    ZSink.fromOutputStreamManaged(os)
  }

  override def getObject(bucket: String, prefix: String, file: Path): Task[Unit] = Task {
    val blobId = BlobId.of(bucket, prefix)
    val blob   = client.get(blobId)
    logger.info(s"Copying object from gs://$bucket/$prefix to local fs $file")
    blob.downloadTo(file)
  }

  override def getObject(bucket: String, prefix: String, chunkSize: Int): GCSStream = {
    val is: Managed[IOException, InputStream] = Managed
      .fromAutoCloseable {
        Task {
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
    ZStream.fromInputStreamManaged(is, chunkSize)
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
    .mapMPar(parallelism) { blob =>
      Task {
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
    listLocalFsObjects(srcPath)
      .mapMPar(parallelism) { path =>
        val targetPath = getTargetPath(srcPath, targetPrefix, path.toString)
        putObject(targetBucket, targetPath, path, opts)
      }
      .runDrain
  }
}

object GCS {

  def listLocalFsObjects(path: String): Stream[Throwable, Path] = Stream
    .fromJavaIterator(Files.walk(Paths.get(path)).iterator())
    .filter(Files.isRegularFile(_))

  def live(path: Option[String] = None): Layer[Throwable, GCSEnv] =
    Managed.effect(GCSClient(path)).map(client => GCS(client)).toLayer
}
