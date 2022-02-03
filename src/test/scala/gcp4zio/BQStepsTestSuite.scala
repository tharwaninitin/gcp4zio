package gcp4zio

import com.google.cloud.bigquery.Schema
import gcp4zio.BQInputType.{CSV, PARQUET}
import utils.Encoder
import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

object BQStepsTestSuite extends GcpTestHelper {
  case class RatingCSV(userId: Long, movieId: Long, rating: Double, timestamp: Long)

  // STEP 1: Define step
  val input_file_parquet  = s"gs://$gcs_bucket/temp/ratings.parquet"
  val input_file_csv      = s"gs://$gcs_bucket/temp/ratings.csv"
  val bq_export_dest_path = s"gs://$gcs_bucket/temp/etlflow/"
  val output_table        = "ratings"
  val output_dataset      = "dev"

  val spec: ZSpec[environment.TestEnvironment with BQEnv, Any] = suite("BQ Steps")(
    testM("Execute BQLoad PARQUET step") {
      val step = BQApi
        .loadIntoBQTable(input_file_parquet, PARQUET, sys.env.get("GCP_PROJECT_ID"), output_dataset, output_table)
      assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    },
    testM("Execute BQLoad CSV step") {
      val schema: Option[Schema] = Encoder[RatingCSV]
      val step =
        BQApi.loadIntoBQTable(input_file_csv, CSV(), sys.env.get("GCP_PROJECT_ID"), output_dataset, output_table, schema = schema)
      assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    },
    testM("Execute BQExport CSV step") {
      val step = BQApi
        .exportFromBQTable(
          sys.env.get("GCP_PROJECT_ID"),
          output_dataset,
          output_table,
          bq_export_dest_path,
          Some("sample.csv"),
          CSV()
        )
      assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    },
    testM("Execute BQExport PARQUET step") {
      val step = BQApi
        .exportFromBQTable(
          sys.env.get("GCP_PROJECT_ID"),
          output_dataset,
          output_table,
          bq_export_dest_path,
          Some("sample.parquet"),
          PARQUET,
          "snappy"
        )
      assertM(step.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    }
  ) @@ TestAspect.sequential
}
