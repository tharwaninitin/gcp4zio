package gcp4zio
package bq

import com.google.cloud.bigquery._
import zio.{RIO, Task, TaskLayer, ZIO, ZLayer}

trait BQ {

  /** Execute SQL query on BigQuery, this API does not returns any data. So it can be used to run any DML/DDL queries
    * @param query
    *   SQL query(INSERT, CREATE) to execute
    * @return
    */
  def executeQuery(query: String): Task[Job]

  /** This API can be used to run any SQL(SELECT) query on BigQuery to fetch rows
    * @param query
    *   SQL query(SELECT) to execute
    * @param fn
    *   function to convert FieldValueList to Scala Type T
    * @tparam T
    *   Scala Type for output rows
    * @return
    */
  def fetchResults[T](query: String)(fn: FieldValueList => T): Task[Iterable[T]]

  /** Load data into BigQuery from GCS
    * @param sourcePath
    *   Source GCS path from which we need to load data into BigQuery
    * @param sourceFormat
    *   File format of source data in GCS
    * @param targetProject
    *   Target Google Project ID
    * @param targetDataset
    *   Target Dataset name
    * @param targetTable
    *   Target Table name
    * @param writeDisposition
    *   Write Disposition for table
    * @param createDisposition
    *   Create Disposition for table
    * @param schema
    *   Schema for source files(Useful in case of CSV and JSON)
    * @return
    */
  def loadTable(
      sourcePath: String,
      sourceFormat: FileType,
      targetProject: scala.Option[String],
      targetDataset: String,
      targetTable: String,
      writeDisposition: JobInfo.WriteDisposition,
      createDisposition: JobInfo.CreateDisposition,
      schema: scala.Option[Schema] = None
  ): Task[Map[String, Long]]

  /** Export data from BigQuery to GCS
    * @param sourceDataset
    *   Source Dataset name
    * @param sourceTable
    *   Source Table name
    * @param sourceProject
    *   Source Google Project ID
    * @param targetPath
    *   Target GCS path
    * @param targetFormat
    *   File format for target GCS location
    * @param targetFileName
    *   Filename in case we want to create single file in target
    * @param targetCompressionType
    *   Compression for destination files
    * @return
    */
  def exportTable(
      sourceDataset: String,
      sourceTable: String,
      sourceProject: scala.Option[String],
      targetPath: String,
      targetFormat: FileType,
      targetFileName: scala.Option[String],
      targetCompressionType: String = "gzip"
  ): Task[Unit]

  def execute[T](f: BigQuery => T): Task[T]
}

object BQ {

  /** Execute SQL query on BigQuery, this API does not returns any data. So it can be used to run any DML/DDL queries
    * @param query
    *   SQL query(INSERT, CREATE) to execute
    * @return
    */
  def executeQuery(query: String): RIO[BQ, Job] = ZIO.environmentWithZIO(_.get.executeQuery(query))

  /** This API can be used to run any SQL(SELECT) query on BigQuery to fetch rows
    * @param query
    *   SQL query(SELECT) to execute
    * @param fn
    *   function to convert FieldValueList to Scala Type T
    * @tparam T
    *   Scala Type for output rows
    * @return
    */
  def fetchResults[T](query: String)(fn: FieldValueList => T): RIO[BQ, Iterable[T]] =
    ZIO.environmentWithZIO(_.get.fetchResults[T](query)(fn))

  /** Load data into BigQuery from GCS
    * @param sourcePath
    *   Source GCS path from which we need to load data into BigQuery
    * @param sourceFormat
    *   File format of source data in GCS
    * @param targetProject
    *   Target Google Project ID
    * @param targetDataset
    *   Target Dataset name
    * @param targetTable
    *   Target Table name
    * @param writeDisposition
    *   Write Disposition for table
    * @param createDisposition
    *   Create Disposition for table
    * @param schema
    *   Schema for source files(Useful in case of CSV and JSON)
    * @return
    */
  def loadTable(
      sourcePath: String,
      sourceFormat: FileType,
      targetProject: scala.Option[String],
      targetDataset: String,
      targetTable: String,
      writeDisposition: JobInfo.WriteDisposition = JobInfo.WriteDisposition.WRITE_TRUNCATE,
      createDisposition: JobInfo.CreateDisposition = JobInfo.CreateDisposition.CREATE_NEVER,
      schema: scala.Option[Schema] = None
  ): RIO[BQ, Map[String, Long]] = ZIO.environmentWithZIO(
    _.get.loadTable(
      sourcePath,
      sourceFormat,
      targetProject,
      targetDataset,
      targetTable,
      writeDisposition,
      createDisposition,
      schema
    )
  )

  /** Load data into BigQuery from GCS
    * @param sourcePathsPartitions
    *   List of source GCS path and partition combination from which we need to load data into BigQuery parallelly
    * @param sourceFormat
    *   File format of source data in GCS
    * @param targetProject
    *   Target Google Project ID
    * @param targetDataset
    *   Target Dataset name
    * @param targetTable
    *   Target Table name
    * @param writeDisposition
    *   Write Disposition for table
    * @param createDisposition
    *   Create Disposition for table
    * @param schema
    *   Schema for source files(Useful in case of CSV and JSON)
    * @param parallelism
    *   Runs with the specified maximum number of fibers for parallel loading into BigQuery.
    * @return
    */
  def loadPartitionedTable(
      sourcePathsPartitions: Seq[(String, String)],
      sourceFormat: FileType,
      targetProject: scala.Option[String],
      targetDataset: String,
      targetTable: String,
      writeDisposition: JobInfo.WriteDisposition,
      createDisposition: JobInfo.CreateDisposition,
      schema: scala.Option[Schema],
      parallelism: Int
  ): RIO[BQ, Map[String, Long]] = ZIO
    .foreachPar(sourcePathsPartitions) { case (srcPath, partition) =>
      loadTable(
        srcPath,
        sourceFormat,
        targetProject,
        targetDataset,
        targetTable + "$" + partition,
        writeDisposition,
        createDisposition,
        schema
      )
    }
    .withParallelism(parallelism)
    .map(x => x.flatten.toMap)

  /** Export data from BigQuery to GCS
    * @param sourceDataset
    *   Source Dataset name
    * @param sourceTable
    *   Source Table name
    * @param sourceProject
    *   Source Google Project ID
    * @param targetPath
    *   Target GCS path
    * @param targetFormat
    *   File format for target GCS location
    * @param targetFileName
    *   Filename in case we want to create single file in target
    * @param targetCompressionType
    *   Compression for destination files
    * @return
    */
  def exportTable(
      sourceDataset: String,
      sourceTable: String,
      sourceProject: scala.Option[String],
      targetPath: String,
      targetFormat: FileType,
      targetFileName: scala.Option[String],
      targetCompressionType: String = "gzip"
  ): RIO[BQ, Unit] = ZIO.environmentWithZIO(
    _.get.exportTable(
      sourceDataset,
      sourceTable,
      sourceProject,
      targetPath,
      targetFormat,
      targetFileName,
      targetCompressionType
    )
  )

  def execute[T](f: BigQuery => T): RIO[BQ, T] = ZIO.environmentWithZIO(_.get.execute(f))

  def live(credentials: scala.Option[String] = None): TaskLayer[BQ] = ZLayer.fromZIO(BQClient(credentials).map(bq => BQImpl(bq)))
}
