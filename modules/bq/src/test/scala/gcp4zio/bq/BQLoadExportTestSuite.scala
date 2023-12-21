package gcp4zio.bq

import com.google.cloud.bigquery.Schema
import gcp4zio.Global.{gcpProject, gcsBucket}
import gcp4zio.bq.FileType.{CSV, PARQUET}
import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test.{assertZIO, suite, test, Spec, TestAspect}

object BQLoadExportTestSuite {
  case class RatingCSV(userId: Long, movieId: Long, rating: Double, timestamp: Long)

  // Define variables
  private val inputFileParquet = s"gs://$gcsBucket/temp/ratings.parquet"
  private val inputFileCsv     = s"gs://$gcsBucket/temp/ratings.csv"
  private val bqExportDestPath = s"gs://$gcsBucket/temp/etlflow/"
  private val outputTable      = "ratings"
  private val outputDataset    = "dev"

  val spec: Spec[BQ, Any] = suite("BQ Load/Export API")(
    test("Run BQLoad PARQUET") {
      val step = BQ.loadTable(inputFileParquet, PARQUET, Some(gcpProject), outputDataset, outputTable)
      assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    },
    test("Run BQLoad CSV") {
      val schema: Option[Schema] = Encoder[RatingCSV]
      val step                   = BQ.loadTable(inputFileCsv, CSV(), Some(gcpProject), outputDataset, outputTable, schema = schema)
      assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    },
    test("Run BQExport CSV") {
      val step = BQ.exportTable(
        outputDataset,
        outputTable,
        Some(gcpProject),
        bqExportDestPath,
        CSV(),
        Some("sample.csv")
      )
      assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    },
    test("Run BQExport PARQUET") {
      val step = BQ.exportTable(
        outputDataset,
        outputTable,
        Some(gcpProject),
        bqExportDestPath,
        PARQUET,
        Some("sample.parquet"),
        "snappy"
      )
      assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    }
  ) @@ TestAspect.sequential
}
