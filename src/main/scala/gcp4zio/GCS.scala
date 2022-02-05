package gcp4zio

import com.google.api.gax.paging.Page
import com.google.cloud.storage.Storage.BlobListOption
import com.google.cloud.storage.{Blob, BlobId, BlobInfo, Storage}
import zio.blocking.Blocking
import zio.stream.ZStream
import zio.{Layer, Managed, Task, ZIO}
import java.io.InputStream
import java.nio.channels.Channels
import java.nio.file.{FileSystems, Files, Path, Paths}
import scala.jdk.CollectionConverters._
import GCS._

case class GCS(client: Storage) extends GCSApi.Service {

  override def listObjects(bucket: String, options: List[BlobListOption]): Task[Page[Blob]] = Task {
    client.list(bucket, options: _*)
  }

  override def listObjects(bucket: String, prefix: String): Task[List[Blob]] = {
    val options: List[BlobListOption] = List(
      // BlobListOption.currentDirectory(),
      BlobListOption.prefix(prefix)
    )
    listObjects(bucket, options)
      .map(_.iterateAll().asScala)
      .map { blobs =>
        // if (blobs.nonEmpty) logger.info("Objects \n"+blobs.mkString("\n"))
        // else logger.info(s"No Objects found under gs://$bucket/$prefix")
        blobs.toList
      }
  }

  override def lookupObject(bucket: String, prefix: String, key: String): Task[Boolean] = {
    val options: List[BlobListOption] = List(
      BlobListOption.prefix(prefix)
    )
    listObjects(bucket, options)
      .map(_.iterateAll().asScala)
      .map { blobs =>
        if (blobs.nonEmpty) logger.info("Objects \n" + blobs.mkString("\n"))
        else logger.info(s"No Objects found under gs://$bucket/$prefix")
        blobs.exists(_.getName == prefix + "/" + key)
      }
  }

  override def putObject(bucket: String, prefix: String, file: Path): Task[Blob] = Task {
    val blobId   = BlobId.of(bucket, prefix)
    val blobInfo = BlobInfo.newBuilder(blobId).build
    logger.info(s"Copying object from local fs $file to gs://$bucket/$prefix")
    client.create(blobInfo, Files.readAllBytes(file))
  }

  override def putObject(bucket: String, prefix: String, file: String): Task[Blob] = for {
    path <- Task(Paths.get(file))
    blob <- putObject(bucket, prefix, path)
  } yield blob

  override def getObject(bucket: String, prefix: String, file: Path): Task[Unit] = Task {
    val blobId = BlobId.of(bucket, prefix)
    val blob   = client.get(blobId)
    logger.info(s"Copying object from gs://$bucket/$prefix to local fs $file")
    blob.downloadTo(file)
  }

  override def getObject(bucket: String, prefix: String, file: String): Task[Unit] = for {
    path <- Task(Paths.get(file))
    _    <- getObject(bucket, prefix, path)
  } yield ()

  override def getObject(bucket: String, prefix: String, chunkSize: Int): ZStream[Blocking, Throwable, Byte] = {
    val is: Managed[Throwable, InputStream] = Managed.fromAutoCloseable {
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
    ZStream.managed(is).flatMap(ZStream.fromInputStream(_, chunkSize))
  }

  override def copyObjectsGCStoGCS(
      src_bucket: String,
      src_prefix: String,
      target_bucket: String,
      target_prefix: String,
      parallelism: Int,
      overwrite: Boolean
  ): Task[Unit] =
    for {
      src_blobs    <- listObjects(src_bucket, src_prefix)
      target_blobs <- listObjects(target_bucket, target_prefix)
      _            <- compareBlobs(src_blobs, src_prefix, target_blobs, target_prefix, overwrite)
      _ <- ZIO.foreachParN_(parallelism)(src_blobs)(blob =>
        Task {
          val target_path = (target_prefix + "/" + blob.getName.replace(src_prefix, "")).replaceAll("//+", "/")
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
  ): Task[Unit] =
    for {
      src_paths <- listLocalFsObjects(src_path)
      _ <- ZIO.foreachParN_(parallelism)(src_paths.toList)(path =>
        for {
          target_path <- Task(getTargetPath(path, src_path, target_prefix))
          _           <- putObject(target_bucket, target_path, path)
        } yield ()
      )
    } yield ()
}

object GCS {
  private def compareBlobs(
      src: List[Blob],
      src_path: String,
      target: List[Blob],
      target_path: String,
      overwrite: Boolean
  ): Task[Unit] = {
    val getName =
      (listOfBlob: List[Blob], pathToReplace: String) => listOfBlob.map(_.getName.replace(pathToReplace, "").replace("/", ""))

    val sourceFileNames = getName(src, src_path)
    val targetFileNames = getName(target, target_path)

    val intersectCount = (sourceFileNames.intersect(targetFileNames)).size

    ZIO.fail(new Exception("File already exists")).unless(intersectCount == 0)
  }.unless(overwrite)

  /** If target path ends with / -> that means directory in that case append file name to it "source_bucket": "local",
    * "source_path": "/path/to/dir/", "target_bucket" : "gcs-bucket", "target_path" : "/remote/path/" val path =
    * getTargetPath("/path/to/dir/file1.txt", "/path/to/dir/", "/remote/path/") path = "/remote/path/file1.txt" Else it means it's
    * a file -> directly return same "source_bucket": "local", "source_path": "/path/to/file/file.txt", "target_bucket" :
    * "gcs-bucket", "target_path" : "/path/file.txt" val path = getTargetPath("/path/to/file/file.txt", "/path/to/file/file.txt",
    * "/path/file.txt") path = "/path/file.txt"
    */
  private def getTargetPath(fileName: Path, srcPath: String, targetPath: String): String = {
    if (targetPath.endsWith("/")) {
      val replaceableString =
        if (srcPath.endsWith("/"))
          srcPath // better approach -> new File(fileStore.sourcePath).isDirectory
        else {
          val splitFilePath = srcPath.split("/")
          splitFilePath.slice(0, splitFilePath.length - 1).mkString("/")
        }
      targetPath + fileName.toString.replace(replaceableString, "")
    } else targetPath
  }.replaceAll("//+", "/")

  private def listLocalFsObjects(path: String): Task[Iterator[Path]] = Task {
    val dir = FileSystems.getDefault.getPath(path)
    Files.walk(dir).iterator().asScala.filter(Files.isRegularFile(_))
  }

  def live(path: Option[String] = None): Layer[Throwable, GCSEnv] =
    Managed.effect(GCSClient(path)).map(client => GCS(client)).toLayer
}
