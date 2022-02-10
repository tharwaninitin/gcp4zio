package gcp4zio

import com.google.cloud.storage.Storage.{BlobListOption, BlobTargetOption, BlobWriteOption}
import com.google.cloud.storage.{Blob, BlobId, BlobInfo, Storage}
import gcp4zio.GCS._
import zio._
import zio.stream._
import java.io.{IOException, InputStream, OutputStream}
import java.nio.channels.Channels
import java.nio.file.{Files, Path, Paths}

case class GCS(client: Storage) extends GCSApi.Service {

  override def listObjects(
      bucket: String,
      prefix: String,
      recursive: Boolean,
      options: List[BlobListOption]
  ): Stream[Throwable, Blob] = {
    val inputOptions    = BlobListOption.prefix(prefix) :: options
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

  override def copyObjectsGCStoGCS(
      src_bucket: String,
      src_prefix: String,
      target_bucket: String,
      target_prefix: String,
      parallelism: Int,
      overwrite: Boolean
  ): Task[Unit] = for {
    src_blobs <- listObjects(src_bucket, src_prefix, recursive = true, List.empty).runCollect
    _ <- ZIO.foreachParN_(parallelism)(src_blobs)(blob =>
      Task {
        val target_path =
          /*This condition is true if single file is passed else directory is passed*/
          if (blob.getName == src_prefix) target_prefix
          else (target_prefix + "/" + blob.getName.replace(src_prefix, "")).replaceAll("//+", "/")
        logger.info(s"Copying object from gs://$src_bucket/${blob.getName} to gs://$target_bucket/$target_path")
        blob.copyTo(target_bucket, target_path)
      }
    )
  } yield ()

  override def copyObjectsLOCALtoGCS(
      src_path: String,
      target_bucket: String,
      target_prefix: String,
      parallelism: Int,
      overwrite: Boolean
  ): Task[Unit] = for {
    src_paths <- listLocalFsObjects(src_path).runCollect
    opts = if (overwrite) List.empty else List(BlobTargetOption.doesNotExist())
    _ <- ZIO.foreachParN_(parallelism)(src_paths)(path =>
      for {
        target_path <- Task {
          /*This condition is true if single file is passed else directory is passed*/
          if (path.toString == src_path) target_prefix
          else (target_prefix + "/" + path.toString.replace(src_path, "")).replaceAll("//+", "/")
        }
        _ <- putObject(target_bucket, target_path, path, opts)
      } yield ()
    )
  } yield ()
}

object GCS {

  def listLocalFsObjects(path: String): Stream[Throwable, Path] = Stream
    .fromJavaIterator(Files.walk(Paths.get(path)).iterator())
    .filter(Files.isRegularFile(_))

  def live(path: Option[String] = None): Layer[Throwable, GCSEnv] =
    Managed.effect(GCSClient(path)).map(client => GCS(client)).toLayer
}
