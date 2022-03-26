package gcp4zio

import com.google.cloud.bigquery.Schema
import gcp4zio.BQInputType.{CSV, PARQUET}
import utils.Encoder
import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

object BQStepsTestSuite extends TestHelper {
  case class RatingCSV(userId: Long, movieId: Long, rating: Double, timestamp: Long)

  // STEP 1: Define step
  val inputFileParquet = s"gs://$gcsBucket/temp/ratings.parquet"
  val inputFileCsv     = s"gs://$gcsBucket/temp/ratings.csv"
  val bqExportDestPath = s"gs://$gcsBucket/temp/etlflow/"
  val outputTable      = "ratings"
  val outputDataset    = "dev"

  val spec: ZSpec[environment.TestEnvironment with BQEnv, Any] = suite("BQ Steps")(
    testM("Execute BQLoad PARQUET step") {
      val step = BQApi.loadTable(inputFileParquet, PARQUET, gcpProjectId, outputDataset, outputTable)
      assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    },
    testM("Execute BQLoad CSV step") {
      val schema: Option[Schema] = Encoder[RatingCSV]
      val step                   = BQApi.loadTable(inputFileCsv, CSV(), gcpProjectId, outputDataset, outputTable, schema = schema)
      assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    },
    testM("Execute BQExport CSV step") {
      val step = BQApi.exportTable(
        outputDataset,
        outputTable,
        gcpProjectId,
        bqExportDestPath,
        Some("sample.csv"),
        CSV()
      )
      assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    },
    testM("Execute BQExport PARQUET step") {
      val step = BQApi.exportTable(
        outputDataset,
        outputTable,
        gcpProjectId,
        bqExportDestPath,
        Some("sample.parquet"),
        PARQUET,
        "snappy"
      )
      assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    }
  ) @@ TestAspect.sequential
}
