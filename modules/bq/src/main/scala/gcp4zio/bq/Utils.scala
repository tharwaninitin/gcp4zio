package gcp4zio.bq

import zio.{Task, ZIO}
import scala.sys.process._

@SuppressWarnings(Array("org.wartremover.warts.ToString", "org.wartremover.warts.Throw"))
object Utils {
  def loadTableFromLocalFile(
      sourceLocations: Either[String, Seq[(String, String)]],
      sourceFormat: FileType,
      targetDataset: String,
      targetTable: String
  ): Task[Unit] = ZIO.attempt {
    sourceLocations match {
      case Left(path) =>
        logger.info("BQ file path: " + path)
        val fullTableName = targetDataset + "." + targetTable
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
          val tablePartition = targetTable + "$" + partition
          val fullTableName  = targetDataset + "." + tablePartition
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
}
