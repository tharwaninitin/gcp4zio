package gcp4zio
package monitoring

import com.google.cloud.monitoring.v3.MetricServiceClient.ListTimeSeriesPagedResponse
import com.google.monitoring.v3.TimeInterval
import zio._

trait MonitoringApi {
  def getMetric(project: String, metric: String, interval: TimeInterval): Task[ListTimeSeriesPagedResponse]
}

object MonitoringApi {
  def getMetric(
      project: String,
      metric: String,
      interval: TimeInterval
  ): ZIO[MonitoringEnv, Throwable, ListTimeSeriesPagedResponse] =
    ZIO.environmentWithZIO(_.get.getMetric(project, metric, interval))
}
