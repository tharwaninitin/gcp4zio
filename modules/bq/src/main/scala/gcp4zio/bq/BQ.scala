package gcp4zio
package bq

import com.google.cloud.bigquery.{FieldValueList, JobInfo, Schema}
import zio.{RIO, Task, TaskLayer, ZIO, ZLayer}

trait BQ {

  /** Execute SQL query on BigQuery, this API does not returns any data. So it can be used to run any DML/DDL queries
    * @param query
    *   SQL query(INSERT, CREATE) to execute
    * @return
    */
  def executeQuery(query: String): Task[Unit]

  /** Execute SQL query on BigQuery, this API returns rows. So it can be used to run any SELECT queries
    * @param query
    *   SQL query(SELECT) to execute
    * @param fn
    *   function to convert FieldValueList to Scala Type T
    * @tparam T
    *   Scala Class for output rows
    * @return
    */
  def getData[T](query: String)(fn: FieldValueList => T): Task[Iterable[T]]

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

  /** Execute SQL query on BigQuery, this API does not returns any data. So it can be used to run any DML/DDL queries
    * @param query
    *   SQL query(INSERT, CREATE) to execute
    * @return
    */
  def executeQuery(query: String): RIO[BQ, Unit] = ZIO.environmentWithZIO(_.get.executeQuery(query))

  /** Execute SQL query on BigQuery, this API returns rows. So it can be used to run any SELECT queries
    * @param query
    *   SQL query(SELECT) to execute
    * @param fn
    *   function to convert FieldValueList to Scala Type T
    * @tparam T
    *   Scala Type for output rows
    * @return
    */
  def getData[T](query: String)(fn: FieldValueList => T): RIO[BQ, Iterable[T]] =
    ZIO.environmentWithZIO(_.get.getData[T](query)(fn))

  def loadTableFromLocalFile(
      sourceLocations: Either[String, Seq[(String, String)]],
      sourceFormat: BQInputType,
      destinationDataset: String,
      destinationTable: String
  ): RIO[BQ, Unit] = ZIO.environmentWithZIO(
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
  ): RIO[BQ, Map[String, Long]] = ZIO.environmentWithZIO(
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
  ): RIO[BQ, Map[String, Long]] = ZIO.environmentWithZIO(
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
  ): RIO[BQ, Unit] = ZIO.environmentWithZIO(
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
