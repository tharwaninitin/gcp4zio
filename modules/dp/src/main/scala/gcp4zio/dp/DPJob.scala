package gcp4zio
package dp

import com.google.cloud.dataproc.v1.Job
import zio.{RIO, Task, TaskLayer, ZIO, ZLayer}
import java.time.Duration

trait DPJob {
  def submitSparkJob(
      args: List[String],
      mainClass: String,
      libs: List[String],
      conf: Map[String, String],
      cluster: String,
      project: String,
      region: String
  ): Task[Job]
  def submitHiveJob(query: String, cluster: String, project: String, region: String): Task[Job]
  def trackJobProgress(project: String, region: String, job: Job, interval: Duration): Task[Unit]
}

object DPJob {
  def submitSparkJob(
      args: List[String],
      mainClass: String,
      libs: List[String],
      conf: Map[String, String],
      cluster: String,
      project: String,
      region: String
  ): RIO[DPJob, Job] =
    ZIO.environmentWithZIO(_.get.submitSparkJob(args, mainClass, libs, conf, cluster, project, region))
  def executeSparkJob(
      args: List[String],
      mainClass: String,
      libs: List[String],
      conf: Map[String, String],
      cluster: String,
      project: String,
      region: String,
      trackingInterval: Duration = Duration.ofSeconds(10)
  ): RIO[DPJob, Job] =
    for {
      job <- ZIO.environmentWithZIO[DPJob](_.get.submitSparkJob(args, mainClass, libs, conf, cluster, project, region))
      _   <- ZIO.environmentWithZIO[DPJob](_.get.trackJobProgress(project, region, job, trackingInterval))
    } yield job
  def submitHiveJob(query: String, clusterName: String, project: String, region: String): RIO[DPJob, Job] =
    ZIO.environmentWithZIO(_.get.submitHiveJob(query, clusterName, project, region))
  def trackJobProgress(
      project: String,
      region: String,
      job: Job,
      interval: Duration = Duration.ofSeconds(10)
  ): RIO[DPJob, Unit] =
    ZIO.environmentWithZIO(_.get.trackJobProgress(project, region, job, interval))
  def live(endpoint: String): TaskLayer[DPJob] = ZLayer.scoped(DPJobClient(endpoint).map(dp => DPJobImpl(dp)))
}
