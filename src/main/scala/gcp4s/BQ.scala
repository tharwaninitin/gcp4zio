package gcp4s

import java.util.UUID
import com.google.cloud.bigquery.{BigQuery, CsvOptions, ExtractJobConfiguration, FieldValueList, FormatOptions, Job, JobConfiguration, JobId, JobInfo, LoadJobConfiguration, QueryJobConfiguration, Schema, StandardTableDefinition, TableId, TableResult}
import BQInputType.{CSV, JSON, ORC, PARQUET}
import zio.{Managed, Task, TaskLayer, ZIO}
import scala.jdk.CollectionConverters._
import scala.sys.process._

case class BQ(client: BigQuery) extends BQApi.Service[Task] {

  private def getFormatOptions(input_type: BQInputType): FormatOptions = input_type match {
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

  def executeQuery(query: String): Task[Unit] = Task {
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

  def getDataFromBQ(query: String): Task[Iterable[FieldValueList]] = Task {
    val queryConfig: QueryJobConfiguration = QueryJobConfiguration
      .newBuilder(query)
      .setUseLegacySql(false)
      .build()

    val jobId    = JobId.of(UUID.randomUUID().toString)
    val queryJob = client.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build())

    // Wait for the query to complete.
    queryJob.waitFor()

    val result: TableResult = queryJob.getQueryResults()
    result.iterateAll().asScala
  }

  def loadIntoBQFromLocalFile(
      source_locations: Either[String, Seq[(String, String)]],
      source_format: BQInputType,
      destination_dataset: String,
      destination_table: String,
      write_disposition: JobInfo.WriteDisposition,
      create_disposition: JobInfo.CreateDisposition
  ): Task[Unit] = Task {
    if (source_locations.isRight) {
      logger.info(s"No of BQ partitions: ${source_locations.getOrElse(Seq.empty).length}")
      source_locations.getOrElse(Seq.empty).foreach { case (src_path, partition) =>
        val table_partition = destination_table + "$" + partition
        val full_table_name = destination_dataset + "." + table_partition
        val bq_load_cmd =
          s"""bq load --replace  --time_partitioning_field date --require_partition_filter=false --source_format=${source_format.toString} $full_table_name $src_path""".stripMargin
        logger.info(s"Loading data from path: $src_path")
        logger.info(s"Destination table: $full_table_name")
        logger.info(s"BQ Load command is: $bq_load_cmd")
        val x = s"$bq_load_cmd".!
        logger.info(s"Output exit code: $x")
        if (x != 0) throw BQLoadException("Error executing BQ load command")
      }
    } else {
      logger.info("BQ file path: " + source_locations.left.getOrElse(""))
      val full_table_name = destination_dataset + "." + destination_table
      val bq_load_cmd =
        s"""bq load --replace --source_format=${source_format.toString} $full_table_name ${source_locations.left
          .getOrElse("")}""".stripMargin
      logger.info(s"Loading data from path: ${source_locations.left.getOrElse("")}")
      logger.info(s"Destination table: $full_table_name")
      logger.info(s"BQ Load command is: $bq_load_cmd")
      val x = s"$bq_load_cmd".!
      logger.info(s"Output exit code: $x")
      if (x != 0) throw BQLoadException("Error executing BQ load command")
    }
  }

  override def loadIntoPartitionedBQTable(
      source_paths_partitions: Seq[(String, String)],
      source_format: BQInputType,
      destination_project: Option[String],
      destination_dataset: String,
      destination_table: String,
      write_disposition: JobInfo.WriteDisposition,
      create_disposition: JobInfo.CreateDisposition,
      schema: Option[Schema],
      parallelism: Int
  ): Task[Map[String, Long]] = {
    logger.info(s"No of BQ partitions: ${source_paths_partitions.length}")
    ZIO
      .foreachParN(parallelism)(source_paths_partitions) { case (src_path, partition) =>
        val table_partition = destination_table + "$" + partition
        loadIntoBQTable(
          src_path,
          source_format,
          destination_project,
          destination_dataset,
          table_partition,
          write_disposition,
          create_disposition
        )
      }
      .map(x => x.flatten.toMap)
  }

  override def loadIntoBQTable(
      source_path: String,
      source_format: BQInputType,
      destination_project: Option[String],
      destination_dataset: String,
      destination_table: String,
      write_disposition: JobInfo.WriteDisposition,
      create_disposition: JobInfo.CreateDisposition,
      schema: Option[Schema]
  ): Task[Map[String, Long]] = Task {
    // Create Output BQ table instance
    val tableId = destination_project match {
      case Some(project) => TableId.of(project, destination_dataset, destination_table)
      case None          => TableId.of(destination_dataset, destination_table)
    }

    val jobConfiguration: JobConfiguration = source_format match {
      case BQInputType.BQ =>
        QueryJobConfiguration
          .newBuilder(source_path)
          .setUseLegacySql(false)
          .setDestinationTable(tableId)
          .setWriteDisposition(write_disposition)
          .setCreateDisposition(create_disposition)
          .setAllowLargeResults(true)
          .build()
      case ORC | PARQUET | CSV(_, _, _, _) =>
        schema match {
          case Some(s) =>
            LoadJobConfiguration
              .builder(tableId, source_path)
              .setFormatOptions(getFormatOptions(source_format))
              .setSchema(s)
              .setWriteDisposition(write_disposition)
              .setCreateDisposition(create_disposition)
              .build()
          case None =>
            LoadJobConfiguration
              .builder(tableId, source_path)
              .setFormatOptions(getFormatOptions(source_format))
              .setWriteDisposition(write_disposition)
              .setCreateDisposition(create_disposition)
              .build()
        }
      case _ => throw BQLoadException("Unsupported Input Type")
    }

    // Create BQ job
    val jobId: JobId = JobId.of(UUID.randomUUID().toString)
    val job: Job     = client.create(JobInfo.newBuilder(jobConfiguration).setJobId(jobId).build())

    // Wait for the job to complete
    val completedJob     = job.waitFor()
    val destinationTable = client.getTable(tableId).getDefinition[StandardTableDefinition]

    if (completedJob.getStatus.getError == null) {
      logger.info(s"Source path: $source_path")
      logger.info(s"Destination table: $destination_dataset.$destination_table")
      logger.info(s"Job State: ${completedJob.getStatus.getState}")
      logger.info(s"Loaded rows: ${destinationTable.getNumRows}")
      logger.info(s"Loaded rows size: ${destinationTable.getNumBytes / 1000000.0} MB")
    } else {
      throw BQLoadException(
        s"""Could not load data in ${source_format.toString} format in table ${destination_dataset + "." + destination_table} due to error ${completedJob.getStatus.getError.getMessage}""".stripMargin
      )
    }
    Map(destination_table -> destinationTable.getNumRows)
  }

  override def exportFromBQTable(
      source_project: Option[String],
      source_dataset: String,
      source_table: String,
      destination_path: String,
      destination_file_name: Option[String],
      destination_format: BQInputType,
      destination_compression_type: String = "gzip"
  ): Task[Unit] = Task {

    val tableId = source_project match {
      case Some(project) => TableId.of(project, source_dataset, source_table)
      case None          => TableId.of(source_dataset, source_table)
    }

    val destinationFormat = destination_format match {
      case CSV(_, _, _, _) => "CSV"
      case PARQUET         => "PARQUET"
      case fmt             => throw BQLoadException(s"Unsupported destination format $fmt")
    }

    val destinationUri =
      destination_path + "/" + destination_file_name.getOrElse(s"part-*.${destinationFormat.toLowerCase}")

    val extractJobConfiguration: ExtractJobConfiguration = destination_format match {
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
          .setCompression(destination_compression_type)
          .build();
      case JSON(_) =>
        ExtractJobConfiguration
          .newBuilder(tableId, destinationUri)
          .setFormat(JSON.toString())
          .setCompression(destination_compression_type)
          .build();
      case _ => throw BQLoadException("Unsupported Destination Format")
    }

    val job          = client.create(JobInfo.of(extractJobConfiguration));
    val completedJob = job.waitFor()

    if (completedJob.getStatus.getError == null) {
      logger.info(s"Source table: $source_dataset.$source_table")
      logger.info(s"Destination path: $destination_path")
      logger.info(s"Job State: ${completedJob.getStatus.getState}")
    } else if (completedJob.getStatus().getError() != null) {
      logger.error(s"BigQuery was unable to extract due to an error:" + job.getStatus().getError())
    } else {
      throw BQLoadException(
        s"""Could not load data from bq table $source_dataset.$source_table to  location  $destination_file_name due to error ${completedJob.getStatus.getError.getMessage}""".stripMargin
      )
    }
  }
}

object BQ {
  def live(credentials: Option[String] = None): TaskLayer[BQEnv] =
    Managed.effect(BQClient(credentials)).map(bq => BQ(bq)).toLayer
}
