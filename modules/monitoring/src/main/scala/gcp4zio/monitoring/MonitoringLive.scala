package gcp4zio
package monitoring

import com.google.cloud.monitoring.v3.MetricServiceClient
import com.google.cloud.monitoring.v3.MetricServiceClient.ListTimeSeriesPagedResponse
import com.google.monitoring.v3.{ListTimeSeriesRequest, ProjectName, TimeInterval}
import zio._

@SuppressWarnings(Array("org.wartremover.warts.ToString"))
case class MonitoringLive(client: MetricServiceClient) extends MonitoringApi {

  override def getMetric(project: String, metric: String, interval: TimeInterval): Task[ListTimeSeriesPagedResponse] =
    ZIO.attempt {
      val name = ProjectName.of(project)
      val requestBuilder =
        ListTimeSeriesRequest
          .newBuilder()
          .setName(name.toString)
          .setFilter(s"""metric.type=\"$metric\"""")
          .setInterval(interval)
          .setView(ListTimeSeriesRequest.TimeSeriesView.HEADERS)
      val request = requestBuilder.build
      logger.info(s"Fetching metric - $metric")
      client.listTimeSeries(request)
    }
}

object MonitoringLive {

  def apply(path: Option[String] = None): Layer[Throwable, MonitoringEnv] =
    ZLayer.fromZIO(ZIO.attempt(MonitoringClient(path)).map(client => MonitoringLive(client)))
}
