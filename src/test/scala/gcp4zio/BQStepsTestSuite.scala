package gcp4zio

import com.google.cloud.bigquery.Schema
import gcp4zio.bq.{BQApi, BQEnv, Encoder}
import gcp4zio.bq.BQInputType.{CSV, PARQUET}
import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

object BQStepsTestSuite extends TestHelper {
  case class RatingCSV(userId: Long, movieId: Long, rating: Double, timestamp: Long)

  // STEP 1: Define step
  private val inputFileParquet = s"gs://$gcsBucket/temp/ratings.parquet"
  private val inputFileCsv     = s"gs://$gcsBucket/temp/ratings.csv"
  private val bqExportDestPath = s"gs://$gcsBucket/temp/etlflow/"
  private val outputTable      = "ratings"
  private val outputDataset    = "dev"

  val spec: Spec[TestEnvironment with BQEnv, Any] = suite("BQ Steps")(
    test("Execute BQLoad PARQUET step") {
      val step = BQApi.loadTable(inputFileParquet, PARQUET, gcpProjectId, outputDataset, outputTable)
      assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    },
    test("Execute BQLoad CSV step") {
      val schema: Option[Schema] = Encoder[RatingCSV]
      val step                   = BQApi.loadTable(inputFileCsv, CSV(), gcpProjectId, outputDataset, outputTable, schema = schema)
      assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    },
    test("Execute BQExport CSV step") {
      val step = BQApi.exportTable(
        outputDataset,
        outputTable,
        gcpProjectId,
        bqExportDestPath,
        Some("sample.csv"),
        CSV()
      )
      assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    },
    test("Execute BQExport PARQUET step") {
      val step = BQApi.exportTable(
        outputDataset,
        outputTable,
        gcpProjectId,
        bqExportDestPath,
        Some("sample.parquet"),
        PARQUET,
        "snappy"
      )
      assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    }
  ) @@ TestAspect.sequential
}
