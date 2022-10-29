package gcp4zio
package bq

import com.google.cloud.bigquery.{FieldValueList, JobInfo, Schema}
import zio.{Task, TaskLayer, ZIO, ZLayer}

trait BQ {
  def executeQuery(query: String): Task[Unit]
  def getData(query: String): Task[Iterable[FieldValueList]]
  def loadTableFromLocalFile(
      sourceLocations: Either[String, Seq[(String, String)]],
      sourceFormat: BQInputType,
      destinationDataset: String,
      destinationTable: String
  ): Task[Unit]
  def loadTable(
      sourcePath: String,
      sourceFormat: BQInputType,
      destinationProject: Option[String],
      destinationDataset: String,
      destinationTable: String,
      writeDisposition: JobInfo.WriteDisposition,
      createDisposition: JobInfo.CreateDisposition,
      schema: Option[Schema] = None
  ): Task[Map[String, Long]]
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
  ): Task[Map[String, Long]]
  def exportTable(
      sourceDataset: String,
      sourceTable: String,
      sourceProject: Option[String],
      destinationPath: String,
      destinationFileName: Option[String],
      destinationFormat: BQInputType,
      destinationCompressionType: String = "gzip"
  ): Task[Unit]
}

object BQ {
  def executeQuery(query: String): ZIO[BQ, Throwable, Unit]                = ZIO.environmentWithZIO(_.get.executeQuery(query))
  def getData(query: String): ZIO[BQ, Throwable, Iterable[FieldValueList]] = ZIO.environmentWithZIO(_.get.getData(query))
  def loadTableFromLocalFile(
      sourceLocations: Either[String, Seq[(String, String)]],
      sourceFormat: BQInputType,
      destinationDataset: String,
      destinationTable: String
  ): ZIO[BQ, Throwable, Unit] =
    ZIO.environmentWithZIO(
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
  ): ZIO[BQ, Throwable, Map[String, Long]] =
    ZIO.environmentWithZIO(
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
  ): ZIO[BQ, Throwable, Map[String, Long]] =
    ZIO.environmentWithZIO(
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
  ): ZIO[BQ, Throwable, Unit] =
    ZIO.environmentWithZIO(
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
  def live(credentials: Option[String] = None): TaskLayer[BQ] = ZLayer.fromZIO(BQClient(credentials).map(bq => BQImpl(bq)))
}
