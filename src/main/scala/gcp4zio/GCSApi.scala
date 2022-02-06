package gcp4zio

import com.google.api.gax.paging.Page
import com.google.cloud.storage.Blob
import com.google.cloud.storage.Storage.{BlobListOption, BlobTargetOption, BlobWriteOption}
import zio.stream.{ZSink, ZStream}
import zio.{Task, ZIO}
import java.nio.file.Path

object GCSApi {
  trait Service {
    def getObject(bucket: String, prefix: String, file: Path): Task[Unit]
    def getObject(bucket: String, prefix: String, chunkSize: Int): GCSStream
    def putObject(bucket: String, prefix: String, file: Path, options: List[BlobTargetOption]): Task[Blob]
    def putObject(bucket: String, prefix: String, options: List[BlobWriteOption]): GCSSink
    def lookupObject(bucket: String, prefix: String): Task[Boolean]
    def listObjects(bucket: String, options: List[BlobListOption]): Task[Page[Blob]]
    def listObjects(bucket: String, prefix: String): Task[List[Blob]]
    def copyObjectsGCStoGCS(
        src_bucket: String,
        src_prefix: String,
        target_bucket: String,
        target_prefix: String,
        parallelism: Int,
        overwrite: Boolean
    ): Task[Unit]
    def copyObjectsLOCALtoGCS(
        src_path: String,
        target_bucket: String,
        target_prefix: String,
        parallelism: Int,
        overwrite: Boolean
    ): Task[Unit]
  }

  def getObject(bucket: String, prefix: String, file: Path): ZIO[GCSEnv, Throwable, Unit] =
    ZIO.accessM(_.get.getObject(bucket, prefix, file))
  def getObject(bucket: String, prefix: String, chunkSize: Int = 4096): GCSStreamWithEnv =
    ZStream.accessStream(_.get.getObject(bucket, prefix, chunkSize))
  def putObject(bucket: String, prefix: String, file: Path, options: List[BlobTargetOption]): ZIO[GCSEnv, Throwable, Blob] =
    ZIO.accessM(_.get.putObject(bucket, prefix, file, options))
  def putObject(bucket: String, prefix: String, options: List[BlobWriteOption]): GCSSinkWithEnv =
    ZSink.accessSink(_.get.putObject(bucket, prefix, options))
  def lookupObject(bucket: String, prefix: String): ZIO[GCSEnv, Throwable, Boolean] =
    ZIO.accessM(_.get.lookupObject(bucket, prefix))
  def listObjects(bucket: String, options: List[BlobListOption]): ZIO[GCSEnv, Throwable, Page[Blob]] =
    ZIO.accessM(_.get.listObjects(bucket, options))
  def listObjects(bucket: String, prefix: String): ZIO[GCSEnv, Throwable, List[Blob]] =
    ZIO.accessM(_.get.listObjects(bucket, prefix))
  def copyObjectsGCStoGCS(
      src_bucket: String,
      src_prefix: String,
      target_bucket: String,
      target_prefix: String,
      parallelism: Int,
      overwrite: Boolean
  ): ZIO[GCSEnv, Throwable, Unit] =
    ZIO.accessM(_.get.copyObjectsGCStoGCS(src_bucket, src_prefix, target_bucket, target_prefix, parallelism, overwrite))
  def copyObjectsLOCALtoGCS(
      src_path: String,
      target_bucket: String,
      target_prefix: String,
      parallelism: Int,
      overwrite: Boolean
  ): ZIO[GCSEnv, Throwable, Unit] =
    ZIO.accessM(_.get.copyObjectsLOCALtoGCS(src_path, target_bucket, target_prefix, parallelism, overwrite))
}
