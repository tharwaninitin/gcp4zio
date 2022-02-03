package gcp4zio

import com.google.cloud.bigquery.{LegacySQLTypeName, Schema}
import utils.Encoder
import zio.test._
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._

object BQSchemaMappingTestSuite {
  case class Ratings1(
      userId: String,
      movieId: Int,
      rating: Long,
      timestamp: Double,
      start_date: java.sql.Date,
      end_date: java.util.Date,
      is_active: Boolean
  )

  class Ratings2(
      val userId: String,
      val movieId: Int,
      val rating: Long,
      val timestamp: Double,
      val start_date: java.sql.Date,
      val end_date: java.util.Date,
      val is_active: Boolean
  )

  val op = ArrayBuffer(
    ("userId", LegacySQLTypeName.STRING),
    ("movieId", LegacySQLTypeName.INTEGER),
    ("rating", LegacySQLTypeName.INTEGER),
    ("timestamp", LegacySQLTypeName.FLOAT),
    ("start_date", LegacySQLTypeName.DATE),
    ("end_date", LegacySQLTypeName.DATE),
    ("is_active", LegacySQLTypeName.BOOLEAN)
  )

  val spec: ZSpec[environment.TestEnvironment, Any] = suite("Encoder Tests")(
    test("Encoder[Ratings1] should return list of field names and field types") {
      val schema: Option[Schema] = Encoder[Ratings1]
      assertTrue(schema.get.getFields.asScala.map(x => (x.getName, x.getType)) == op)
    },
    test("Encoder[Ratings2] should return list of field names and field types") {
      val schema: Option[Schema] = Encoder[Ratings2]
      assertTrue(schema.get.getFields.asScala.map(x => (x.getName, x.getType)) == op)
    }
  )
}
