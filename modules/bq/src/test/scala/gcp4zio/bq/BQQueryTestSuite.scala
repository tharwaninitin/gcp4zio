package gcp4zio.bq

import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

object BQQueryTestSuite {
  case class Rating(userId: Long, movieId: Long, rating: Double, timestamp: Long)

  val spec: Spec[BQ, Any] = suite("BQ Query API")(
    test("Execute BQ Query") {
      val task = BQ.executeQuery("")
      assertZIO(task.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    },
    test("Execute BQ Query and get data") {
      val task =
        BQ.fetchResults("SELECT * FROM dev.ratings")(rs =>
          Rating(
            rs.get("userId").getLongValue,
            rs.get("movieId").getLongValue,
            rs.get("rating").getDoubleValue,
            rs.get("timestamp").getLongValue
          )
        )
      assertZIO(task.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
    }
  ) @@ TestAspect.sequential
}
