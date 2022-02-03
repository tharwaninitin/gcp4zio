package gcp4zio

import com.google.cloud.bigquery.{FieldValueList, JobInfo, Schema}
import zio.ZIO

object BQApi {
  trait Service[F[_]] {
    def executeQuery(query: String): F[Unit]
    def getData(query: String): F[Iterable[FieldValueList]]
    def loadTableFromLocalFile(
        source_locations: Either[String, Seq[(String, String)]],
        source_format: BQInputType,
        destination_dataset: String,
        destination_table: String
    ): F[Unit]
    def loadTable(
        source_path: String,
        source_format: BQInputType,
        destination_project: Option[String],
        destination_dataset: String,
        destination_table: String,
        write_disposition: JobInfo.WriteDisposition,
        create_disposition: JobInfo.CreateDisposition,
        schema: Option[Schema] = None
    ): F[Map[String, Long]]
    def loadPartitionedTable(
        source_paths_partitions: Seq[(String, String)],
        source_format: BQInputType,
        destination_project: Option[String],
        destination_dataset: String,
        destination_table: String,
        write_disposition: JobInfo.WriteDisposition,
        create_disposition: JobInfo.CreateDisposition,
        schema: Option[Schema],
        parallelism: Int
    ): F[Map[String, Long]]
    def exportTable(
        source_dataset: String,
        source_table: String,
        source_project: Option[String],
        destination_path: String,
        destination_file_name: Option[String],
        destination_format: BQInputType,
        destination_compression_type: String = "gzip"
    ): F[Unit]
  }

  def executeQuery(query: String): ZIO[BQEnv, Throwable, Unit]                = ZIO.accessM(_.get.executeQuery(query))
  def getData(query: String): ZIO[BQEnv, Throwable, Iterable[FieldValueList]] = ZIO.accessM(_.get.getData(query))
  def loadTableFromLocalFile(
      source_locations: Either[String, Seq[(String, String)]],
      source_format: BQInputType,
      destination_dataset: String,
      destination_table: String
  ): ZIO[BQEnv, Throwable, Unit] =
    ZIO.accessM(
      _.get.loadTableFromLocalFile(
        source_locations,
        source_format,
        destination_dataset,
        destination_table
      )
    )
  def loadTable(
      source_path: String,
      source_format: BQInputType,
      destination_project: Option[String],
      destination_dataset: String,
      destination_table: String,
      write_disposition: JobInfo.WriteDisposition = JobInfo.WriteDisposition.WRITE_TRUNCATE,
      create_disposition: JobInfo.CreateDisposition = JobInfo.CreateDisposition.CREATE_NEVER,
      schema: Option[Schema] = None
  ): ZIO[BQEnv, Throwable, Map[String, Long]] =
    ZIO.accessM(
      _.get.loadTable(
        source_path,
        source_format,
        destination_project,
        destination_dataset,
        destination_table,
        write_disposition,
        create_disposition,
        schema
      )
    )
  def loadPartitionedTable(
      source_paths_partitions: Seq[(String, String)],
      source_format: BQInputType,
      destination_project: Option[String],
      destination_dataset: String,
      destination_table: String,
      write_disposition: JobInfo.WriteDisposition,
      create_disposition: JobInfo.CreateDisposition,
      schema: Option[Schema],
      parallelism: Int
  ): ZIO[BQEnv, Throwable, Map[String, Long]] =
    ZIO.accessM(
      _.get.loadPartitionedTable(
        source_paths_partitions,
        source_format,
        destination_project,
        destination_dataset,
        destination_table,
        write_disposition,
        create_disposition,
        schema,
        parallelism
      )
    )
  def exportTable(
      source_dataset: String,
      source_table: String,
      source_project: Option[String],
      destination_path: String,
      destination_file_name: Option[String],
      destination_format: BQInputType,
      destination_compression_type: String = "gzip"
  ): ZIO[BQEnv, Throwable, Unit] =
    ZIO.accessM(
      _.get.exportTable(
        source_dataset,
        source_table,
        source_project,
        destination_path,
        destination_file_name,
        destination_format,
        destination_compression_type
      )
    )
}
