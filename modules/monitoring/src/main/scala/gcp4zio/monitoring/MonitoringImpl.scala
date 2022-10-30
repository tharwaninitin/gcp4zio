package gcp4zio
package monitoring

import com.google.cloud.monitoring.v3.MetricServiceClient
import com.google.monitoring.v3.{ListTimeSeriesRequest, ProjectName, TimeInterval, TimeSeries}
import zio._
import scala.jdk.CollectionConverters._

@SuppressWarnings(Array("org.wartremover.warts.ToString"))
case class MonitoringImpl(client: MetricServiceClient) extends Monitoring {
  override def getMetric(project: String, metric: String, interval: TimeInterval): Task[Iterable[TimeSeries]] = ZIO.attempt {
    val name = ProjectName.of(project)
    val requestBuilder = ListTimeSeriesRequest
      .newBuilder()
      .setName(name.toString)
      .setFilter(s"""metric.type="$metric"""")
      .setInterval(interval)
      .setView(ListTimeSeriesRequest.TimeSeriesView.HEADERS)
    val request = requestBuilder.build
    logger.info(s"Fetching metric - $metric")
    client.listTimeSeries(request).iterateAll.asScala
  }
}
