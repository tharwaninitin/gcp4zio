package gcp4zio

import com.google.monitoring.v3.TimeInterval
import com.google.protobuf.util.Timestamps
import gcp4zio.Global._
import gcp4zio.monitoring.{MonitoringApi, MonitoringEnv}
import zio.ZIO
import zio.test.Assertion.{containsString, equalTo}
import zio.test._

object MonitoringTestSuite {
  val spec: Spec[MonitoringEnv, Any] =
    suite("Monitoring APIs")(
      test("Query PubSub Subscription's number of undelivered messages Metric") {
        val currentTime = System.currentTimeMillis
        val interval = TimeInterval.newBuilder
          .setStartTime(Timestamps.fromMillis(currentTime - 61000))
          .setEndTime(Timestamps.fromMillis(currentTime))
          .build
        val step = MonitoringApi.getMetric(project, metric, interval)
        assertZIO(step.foldZIO(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Query Not-existing Metric") {
        val currentTime = System.currentTimeMillis
        val interval = TimeInterval.newBuilder
          .setStartTime(Timestamps.fromMillis(currentTime - 61000))
          .setEndTime(Timestamps.fromMillis(currentTime))
          .build
        val step  = MonitoringApi.getMetric(project, notExistingMetric, interval)
        val error = "NOT_FOUND"
        assertZIO(step.foldZIO(ex => ZIO.succeed(ex.getMessage), _ => ZIO.fail("ok")))(containsString(error))
      }
    ) @@ TestAspect.sequential
}
