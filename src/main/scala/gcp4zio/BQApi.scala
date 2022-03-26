package gcp4zio

import com.google.cloud.bigquery.{FieldValueList, JobInfo, Schema}
import zio.ZIO

object BQApi {
  trait Service[F[_]] {
    def executeQuery(query: String): F[Unit]
    def getData(query: String): F[Iterable[FieldValueList]]
    def loadTableFromLocalFile(
        sourceLocations: Either[String, Seq[(String, String)]],
        sourceFormat: BQInputType,
        destinationDataset: String,
        destinationTable: String
    ): F[Unit]
    def loadTable(
        sourcePath: String,
        sourceFormat: BQInputType,
        destinationProject: Option[String],
        destinationDataset: String,
        destinationTable: String,
        writeDisposition: JobInfo.WriteDisposition,
        createDisposition: JobInfo.CreateDisposition,
        schema: Option[Schema] = None
    ): F[Map[String, Long]]
    def loadPartitionedTable(
        sourcePathsPartitions: Seq[(String, String)],
        sourceFormat: BQInputType,
        destinationProject: Option[String],
        destinationDataset: String,
        destinationTable: String,
        writeDisposition: JobInfo.WriteDisposition,
        createDisposition: JobInfo.CreateDisposition,
        schema: Option[Schema],
        parallelism: Int
    ): F[Map[String, Long]]
    def exportTable(
        sourceDataset: String,
        sourceTable: String,
        sourceProject: Option[String],
        destinationPath: String,
        destinationFileName: Option[String],
        destinationFormat: BQInputType,
        destinationCompressionType: String = "gzip"
    ): F[Unit]
  }

  def executeQuery(query: String): ZIO[BQEnv, Throwable, Unit]                = ZIO.accessM(_.get.executeQuery(query))
  def getData(query: String): ZIO[BQEnv, Throwable, Iterable[FieldValueList]] = ZIO.accessM(_.get.getData(query))
  def loadTableFromLocalFile(
      sourceLocations: Either[String, Seq[(String, String)]],
      sourceFormat: BQInputType,
      destinationDataset: String,
      destinationTable: String
  ): ZIO[BQEnv, Throwable, Unit] =
    ZIO.accessM(
      _.get.loadTableFromLocalFile(
        sourceLocations,
        sourceFormat,
        destinationDataset,
        destinationTable
      )
    )
  def loadTable(
      sourcePath: String,
      sourceFormat: BQInputType,
      destinationProject: Option[String],
      destinationDataset: String,
      destinationTable: String,
      writeDisposition: JobInfo.WriteDisposition = JobInfo.WriteDisposition.WRITE_TRUNCATE,
      createDisposition: JobInfo.CreateDisposition = JobInfo.CreateDisposition.CREATE_NEVER,
      schema: Option[Schema] = None
  ): ZIO[BQEnv, Throwable, Map[String, Long]] =
    ZIO.accessM(
      _.get.loadTable(
        sourcePath,
        sourceFormat,
        destinationProject,
        destinationDataset,
        destinationTable,
        writeDisposition,
        createDisposition,
        schema
      )
    )
  def loadPartitionedTable(
      sourcePathsPartitions: Seq[(String, String)],
      sourceFormat: BQInputType,
      destinationProject: Option[String],
      destinationDataset: String,
      destinationTable: String,
      writeDisposition: JobInfo.WriteDisposition,
      createDisposition: JobInfo.CreateDisposition,
      schema: Option[Schema],
      parallelism: Int
  ): ZIO[BQEnv, Throwable, Map[String, Long]] =
    ZIO.accessM(
      _.get.loadPartitionedTable(
        sourcePathsPartitions,
        sourceFormat,
        destinationProject,
        destinationDataset,
        destinationTable,
        writeDisposition,
        createDisposition,
        schema,
        parallelism
      )
    )
  def exportTable(
      sourceDataset: String,
      sourceTable: String,
      sourceProject: Option[String],
      destinationPath: String,
      destinationFileName: Option[String],
      destinationFormat: BQInputType,
      destinationCompressionType: String = "gzip"
  ): ZIO[BQEnv, Throwable, Unit] =
    ZIO.accessM(
      _.get.exportTable(
        sourceDataset,
        sourceTable,
        sourceProject,
        destinationPath,
        destinationFileName,
        destinationFormat,
        destinationCompressionType
      )
    )
}
