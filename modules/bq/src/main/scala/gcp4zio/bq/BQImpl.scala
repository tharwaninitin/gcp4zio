package gcp4zio
package bq

import com.google.cloud.bigquery._
import gcp4zio.bq.BQInputType.{CSV, JSON, ORC, PARQUET}
import zio.{Task, ZIO}
import java.util.UUID
import scala.jdk.CollectionConverters._
import scala.sys.process._

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

  private def getFormatOptions(inputType: BQInputType): FormatOptions = inputType match {
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

  def executeQuery(query: String): Task[Unit] = ZIO.attempt {
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
      throw new RuntimeException(s"Error ${queryJob.getStatus.getError.getMessage}")
    } else {
      logger.info(s"Job State: ${queryJob.getStatus.getState}")
      // val stats = queryJob.getStatistics.asInstanceOf[QueryStatistics]
      // query_logger.info(s"Query Plan : ${stats.getQueryPlan}")
    }
  }

  def getData(query: String): Task[Iterable[FieldValueList]] = ZIO.attempt {
    val queryConfig: QueryJobConfiguration = QueryJobConfiguration
      .newBuilder(query)
      .setUseLegacySql(false)
      .build()

    val jobId    = JobId.of(UUID.randomUUID().toString)
    val queryJob = client.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build())

    // Wait for the query to complete.
    val job = queryJob.waitFor()

    val result: TableResult = job.getQueryResults()
    result.iterateAll().asScala
  }

  def loadTableFromLocalFile(
      sourceLocations: Either[String, Seq[(String, String)]],
      sourceFormat: BQInputType,
      destinationDataset: String,
      destinationTable: String
  ): Task[Unit] = ZIO.attempt {
    sourceLocations match {
      case Left(path) =>
        logger.info("BQ file path: " + path)
        val fullTableName = destinationDataset + "." + destinationTable
        val bqLoadCmd =
          s"""bq load --replace --source_format=${sourceFormat.toString} $fullTableName $path""".stripMargin
        logger.info(s"Loading data from path: $path")
        logger.info(s"Destination table: $fullTableName")
        logger.info(s"BQ Load command is: $bqLoadCmd")
        val x = s"$bqLoadCmd".!
        logger.info(s"Output exit code: $x")
        if (x != 0) throw BQLoadException("Error executing BQ load command")
      case Right(list) =>
        logger.info(s"No of BQ partitions: ${list.length}")
        list.foreach { case (src_path, partition) =>
          val tablePartition = destinationTable + "$" + partition
          val fullTableName  = destinationDataset + "." + tablePartition
          val bqLoadCmd =
            s"""bq load --replace --time_partitioning_field date --require_partition_filter=false --source_format=${sourceFormat.toString} $fullTableName $src_path""".stripMargin
          logger.info(s"Loading data from path: $src_path")
          logger.info(s"Destination table: $fullTableName")
          logger.info(s"BQ Load command is: $bqLoadCmd")
          val x = s"$bqLoadCmd".!
          logger.info(s"Output exit code: $x")
          if (x != 0) throw BQLoadException("Error executing BQ load command")
        }
    }
  }

  override def loadPartitionedTable(
      sourcePathsPartitions: Seq[(String, String)],
      sourceFormat: BQInputType,
      destinationProject: scala.Option[String],
      destinationDataset: String,
      destinationTable: String,
      writeDisposition: JobInfo.WriteDisposition,
      createDisposition: JobInfo.CreateDisposition,
      schema: scala.Option[Schema],
      parallelism: Int
  ): Task[Map[String, Long]] = {
    logger.info(s"No of BQ partitions: ${sourcePathsPartitions.length}")
    ZIO
      .foreachPar(sourcePathsPartitions) { case (src_path, partition) =>
        loadTable(
          src_path,
          sourceFormat,
          destinationProject,
          destinationDataset,
          destinationTable + "$" + partition,
          writeDisposition,
          createDisposition
        )
      }
      .withParallelism(parallelism)
      .map(x => x.flatten.toMap)
  }

  override def loadTable(
      sourcePath: String,
      sourceFormat: BQInputType,
      destinationProject: scala.Option[String],
      destinationDataset: String,
      destinationTable: String,
      writeDisposition: JobInfo.WriteDisposition,
      createDisposition: JobInfo.CreateDisposition,
      schema: scala.Option[Schema]
  ): Task[Map[String, Long]] = ZIO.attempt {
    val tableId = destinationProject match {
      case Some(project) => TableId.of(project, destinationDataset, destinationTable)
      case None          => TableId.of(destinationDataset, destinationTable)
    }

    val jobConfiguration: JobConfiguration = sourceFormat match {
      case BQInputType.BQ =>
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
    val completedJob        = job.waitFor()
    val destinationTableDef = client.getTable(tableId).getDefinition[StandardTableDefinition]

    if (completedJob.getStatus.getError == null) {
      logger.info(s"Source path: $sourcePath")
      logger.info(s"Destination table: $destinationDataset.$destinationTable")
      logger.info(s"Job State: ${completedJob.getStatus.getState}")
      logger.info(s"Loaded rows: ${destinationTableDef.getNumRows}")
      logger.info(s"Loaded rows size: ${destinationTableDef.getNumBytes / 1000000.0} MB")
    } else {
      throw BQLoadException(
        s"""Could not load data in ${sourceFormat.toString} format in table ${destinationDataset + "." + destinationTable} due to error ${completedJob.getStatus.getError.getMessage}""".stripMargin
      )
    }
    Map(destinationTable -> destinationTableDef.getNumRows)
  }

  override def exportTable(
      sourceDataset: String,
      sourceTable: String,
      sourceProject: scala.Option[String],
      destinationPath: String,
      destinationFileName: scala.Option[String],
      destinationFormat: BQInputType,
      destinationCompressionType: String = "gzip"
  ): Task[Unit] = ZIO.attempt {

    val tableId = sourceProject match {
      case Some(project) => TableId.of(project, sourceDataset, sourceTable)
      case None          => TableId.of(sourceDataset, sourceTable)
    }

    val destinationFormatStr = destinationFormat match {
      case CSV(_, _, _, _) => "CSV"
      case PARQUET         => "PARQUET"
      case fmt             => throw BQLoadException(s"Unsupported destination format $fmt")
    }

    val destinationUri =
      destinationPath + "/" + destinationFileName.getOrElse(s"part-*.${destinationFormatStr.toLowerCase}")

    val extractJobConfiguration: ExtractJobConfiguration = destinationFormat match {
      case CSV(delimiter, _, _, _) =>
        ExtractJobConfiguration
          .newBuilder(tableId, destinationUri)
          .setFormat(CSV.toString())
          .setFieldDelimiter(delimiter)
          .build();
      case PARQUET =>
        ExtractJobConfiguration
          .newBuilder(tableId, destinationUri)
          .setFormat(PARQUET.toString)
          .setCompression(destinationCompressionType)
          .build();
      case JSON(_) =>
        ExtractJobConfiguration
          .newBuilder(tableId, destinationUri)
          .setFormat(JSON.toString())
          .setCompression(destinationCompressionType)
          .build();
      case _ => throw BQLoadException("Unsupported Destination Format")
    }

    val job = client.create(JobInfo.of(extractJobConfiguration))

    val completedJob = job.waitFor()

    if (completedJob.getStatus.getError == null) {
      logger.info(s"Source table: $sourceDataset.$sourceTable")
      logger.info(s"Destination path: $destinationPath")
      logger.info(s"Job State: ${completedJob.getStatus.getState}")
    } else if (completedJob.getStatus.getError != null) {
      logger.error(s"BigQuery was unable to extract due to an error:" + job.getStatus.getError)
    } else {
      throw BQLoadException(
        s"""Could not load data from bq table $sourceDataset.$sourceTable to  location  $destinationFileName due to error ${completedJob.getStatus.getError.getMessage}""".stripMargin
      )
    }
  }
}
