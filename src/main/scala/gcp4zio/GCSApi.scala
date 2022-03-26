package gcp4zio

import com.google.cloud.storage.Blob
import com.google.cloud.storage.Storage.{BlobListOption, BlobTargetOption, BlobWriteOption}
import zio.stream._
import zio._
import java.nio.file.Path

object GCSApi {
  trait Service {
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
  }

  def getObject(bucket: String, prefix: String, file: Path): ZIO[GCSEnv, Throwable, Unit] =
    ZIO.accessM(_.get.getObject(bucket, prefix, file))
  def getObject(bucket: String, prefix: String, chunkSize: Int): GCSStreamWithEnv =
    ZStream.accessStream(_.get.getObject(bucket, prefix, chunkSize))
  def putObject(bucket: String, prefix: String, file: Path, options: List[BlobTargetOption]): ZIO[GCSEnv, Throwable, Blob] =
    ZIO.accessM(_.get.putObject(bucket, prefix, file, options))
  def putObject(bucket: String, prefix: String, options: List[BlobWriteOption]): GCSSinkWithEnv =
    ZSink.accessSink(_.get.putObject(bucket, prefix, options))
  def lookupObject(bucket: String, prefix: String): ZIO[GCSEnv, Throwable, Boolean] =
    ZIO.accessM(_.get.lookupObject(bucket, prefix))
  def deleteObject(bucket: String, prefix: String): ZIO[GCSEnv, Throwable, Boolean] =
    ZIO.accessM(_.get.deleteObject(bucket, prefix))
  def listObjects(
      bucket: String,
      prefix: Option[String] = None,
      recursive: Boolean = true,
      options: List[BlobListOption] = List.empty
  ): ZStream[GCSEnv, Throwable, Blob] =
    ZStream.accessStream(_.get.listObjects(bucket, prefix, recursive, options))
  def copyObjectsGCStoGCS(
      srcBucket: String,
      srcPrefix: Option[String] = None,
      srcRecursive: Boolean = true,
      srcOptions: List[BlobListOption] = List.empty,
      targetBucket: String,
      targetPrefix: Option[String] = None,
      parallelism: Int = 2
  ): ZIO[GCSEnv, Throwable, Unit] =
    ZIO.accessM(
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
    ZIO.accessM(_.get.copyObjectsLOCALtoGCS(srcPath, targetBucket, targetPrefix, parallelism, overwrite))
}
