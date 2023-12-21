package gcp4zio
package bq

import com.google.cloud.bigquery._
import gcp4zio.bq.FileType.{CSV, JSON, ORC, PARQUET}
import zio.{Task, ZIO}
import java.util.UUID
import scala.jdk.CollectionConverters._

@SuppressWarnings(
  Array(
    "org.wartremover.warts.Var",
    "org.wartremover.warts.ToString",
    "org.wartremover.warts.Throw",
    "org.wartremover.warts.PlatformDefault",
    "org.wartremover.warts.AutoUnboxing"
  )
)
case class BQImpl(client: BigQuery) extends BQ {

  private def getFormatOptions(inputType: FileType): FormatOptions = inputType match {
    case PARQUET => FormatOptions.parquet
    case ORC     => FormatOptions.orc
    case CSV(field_delimiter, header_present, _, _) =>
      CsvOptions
        .newBuilder()
        .setSkipLeadingRows(if (header_present) 1 else 0)
        .setFieldDelimiter(field_delimiter)
        .build()
    case _ => FormatOptions.parquet
  }

  /** Execute SQL query on BigQuery, this API does not returns any data. So it can be used to run any DML/DDL queries
    * @param query
    *   SQL query(INSERT, CREATE) to execute
    * @return
    */
  def executeQuery(query: String): Task[Job] = ZIO.attempt {
    val queryConfig: QueryJobConfiguration = QueryJobConfiguration
      .newBuilder(query)
      .setUseLegacySql(false)
      .build()

    val jobId    = JobId.of(UUID.randomUUID().toString)
    var queryJob = client.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build())

    // Wait for the query to complete.
    try queryJob = queryJob.waitFor()
    catch {
      case e: InterruptedException =>
        e.printStackTrace()
    }

    if (queryJob == null)
      throw new RuntimeException("Job no longer exists")
    else if (queryJob.getStatus.getError != null) {
      logger.error(queryJob.getStatus.getState.toString)
      logger.info(s"EmailId: ${queryJob.getUserEmail}")
      throw new RuntimeException(s"Error ${queryJob.getStatus.getError.getMessage}")
    } else {
      logger.info(s"Executed query successfully")
      queryJob
    }
  }

  /** This API can be used to run any SQL(SELECT) query on BigQuery to fetch rows
    * @param query
    *   SQL query(SELECT) to execute
    * @param fn
    *   function to convert FieldValueList to Scala Type T
    * @tparam T
    *   Scala Type for output rows
    * @return
    */
  def fetchResults[T](query: String)(fn: FieldValueList => T): Task[Iterable[T]] = ZIO.attempt {
    val queryConfig: QueryJobConfiguration = QueryJobConfiguration
      .newBuilder(query)
      .setUseLegacySql(false)
      .build()

    val jobId    = JobId.of(UUID.randomUUID().toString)
    val queryJob = client.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build())

    // Wait for the query to complete.
    val job = queryJob.waitFor()

    val result: TableResult = job.getQueryResults()
    result.iterateAll().asScala.map(fn)
  }

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
  override def loadTable(
      sourcePath: String,
      sourceFormat: FileType,
      targetProject: scala.Option[String],
      targetDataset: String,
      targetTable: String,
      writeDisposition: JobInfo.WriteDisposition,
      createDisposition: JobInfo.CreateDisposition,
      schema: scala.Option[Schema]
  ): Task[Map[String, Long]] = ZIO.attempt {
    val tableId = targetProject match {
      case Some(project) => TableId.of(project, targetDataset, targetTable)
      case None          => TableId.of(targetDataset, targetTable)
    }

    val jobConfiguration: JobConfiguration = sourceFormat match {
      case FileType.BQ =>
        QueryJobConfiguration
          .newBuilder(sourcePath)
          .setUseLegacySql(false)
          .setDestinationTable(tableId)
          .setWriteDisposition(writeDisposition)
          .setCreateDisposition(createDisposition)
          .setAllowLargeResults(true)
          .build()
      case ORC | PARQUET | CSV(_, _, _, _) =>
        schema match {
          case Some(s) =>
            LoadJobConfiguration
              .builder(tableId, sourcePath)
              .setFormatOptions(getFormatOptions(sourceFormat))
              .setSchema(s)
              .setWriteDisposition(writeDisposition)
              .setCreateDisposition(createDisposition)
              .build()
          case None =>
            LoadJobConfiguration
              .builder(tableId, sourcePath)
              .setFormatOptions(getFormatOptions(sourceFormat))
              .setWriteDisposition(writeDisposition)
              .setCreateDisposition(createDisposition)
              .build()
        }
      case _ => throw BQLoadException("Unsupported Input Type")
    }

    // Create BQ job
    val jobId: JobId = JobId.of(UUID.randomUUID().toString)
    val job: Job     = client.create(JobInfo.newBuilder(jobConfiguration).setJobId(jobId).build())

    // Wait for the job to complete
    val completedJob   = job.waitFor()
    val targetTableDef = client.getTable(tableId).getDefinition[StandardTableDefinition]

    if (completedJob.getStatus.getError == null) {
      logger.info(s"Source path: $sourcePath")
      logger.info(s"Destination table: $targetDataset.$targetTable")
      logger.info(s"Job State: ${completedJob.getStatus.getState}")
      logger.info(s"Loaded rows: ${targetTableDef.getNumRows}")
      logger.info(s"Loaded rows size: ${targetTableDef.getNumBytes / 1000000.0} MB")
    } else {
      throw BQLoadException(
        s"""Could not load data in ${sourceFormat.toString} format in table ${targetDataset + "." + targetTable} due to error ${completedJob.getStatus.getError.getMessage}""".stripMargin
      )
    }
    Map(targetTable -> targetTableDef.getNumRows)
  }

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
  override def exportTable(
      sourceDataset: String,
      sourceTable: String,
      sourceProject: scala.Option[String],
      targetPath: String,
      targetFormat: FileType,
      targetFileName: scala.Option[String],
      targetCompressionType: String = "gzip"
  ): Task[Unit] = ZIO.attempt {

    val tableId = sourceProject match {
      case Some(project) => TableId.of(project, sourceDataset, sourceTable)
      case None          => TableId.of(sourceDataset, sourceTable)
    }

    val targetFormatStr = targetFormat match {
      case CSV(_, _, _, _) => "CSV"
      case PARQUET         => "PARQUET"
      case fmt             => throw BQLoadException(s"Unsupported target format $fmt")
    }

    val targetUri =
      targetPath + "/" + targetFileName.getOrElse(s"part-*.${targetFormatStr.toLowerCase}")

    val extractJobConfiguration: ExtractJobConfiguration = targetFormat match {
      case CSV(delimiter, _, _, _) =>
        ExtractJobConfiguration
          .newBuilder(tableId, targetUri)
          .setFormat(CSV.toString())
          .setFieldDelimiter(delimiter)
          .build();
      case PARQUET =>
        ExtractJobConfiguration
          .newBuilder(tableId, targetUri)
          .setFormat(PARQUET.toString)
          .setCompression(targetCompressionType)
          .build();
      case JSON(_) =>
        ExtractJobConfiguration
          .newBuilder(tableId, targetUri)
          .setFormat(JSON.toString())
          .setCompression(targetCompressionType)
          .build();
      case _ => throw BQLoadException("Unsupported Destination Format")
    }

    val job = client.create(JobInfo.of(extractJobConfiguration))

    val completedJob = job.waitFor()

    if (completedJob.getStatus.getError == null) {
      logger.info(s"Source table: $sourceDataset.$sourceTable")
      logger.info(s"Destination path: $targetPath")
      logger.info(s"Job State: ${completedJob.getStatus.getState}")
    } else if (completedJob.getStatus.getError != null) {
      logger.error(s"BigQuery was unable to extract due to an error:" + job.getStatus.getError)
    } else {
      throw BQLoadException(
        s"""Could not load data from BQ table $sourceDataset.$sourceTable to location  $targetFileName due to error ${completedJob.getStatus.getError.getMessage}""".stripMargin
      )
    }
  }

  /** Execute function with BigQuery as Input and return Generic o/p T
    *
    * @param f
    *   BigQuery => T
    * @tparam T
    *   Output
    * @return
    */
  override def execute[T](f: BigQuery => T): Task[T] = ZIO.attempt(f(client))
}
