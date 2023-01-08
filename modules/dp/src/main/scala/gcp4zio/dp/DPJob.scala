package gcp4zio
package dp

import com.google.cloud.dataproc.v1.Job
import zio.{RIO, Task, TaskLayer, ZIO, ZLayer}
import java.time.Duration

trait DPJob {
  def submitSparkJob(args: List[String], mainClass: String, libs: List[String], conf: Map[String, String]): Task[Job]
  def submitHiveJob(query: String): Task[Job]
  def trackJobProgress(job: Job, interval: Duration): Task[Unit]
}

object DPJob {
  def submitSparkJob(args: List[String], mainClass: String, libs: List[String], conf: Map[String, String]): RIO[DPJob, Job] =
    ZIO.environmentWithZIO(_.get.submitSparkJob(args, mainClass, libs, conf))

  def executeSparkJob(
      args: List[String],
      mainClass: String,
      libs: List[String],
      conf: Map[String, String],
      trackingInterval: Duration = Duration.ofSeconds(10)
  ): RIO[DPJob, Job] =
    for {
      job <- ZIO.environmentWithZIO[DPJob](_.get.submitSparkJob(args, mainClass, libs, conf))
      _   <- ZIO.environmentWithZIO[DPJob](_.get.trackJobProgress(job, trackingInterval))
    } yield job

  def submitHiveJob(query: String): RIO[DPJob, Job] = ZIO.environmentWithZIO(_.get.submitHiveJob(query))

  def executeHiveJob(query: String, trackingInterval: Duration = Duration.ofSeconds(10)): RIO[DPJob, Job] =
    for {
      job <- ZIO.environmentWithZIO[DPJob](_.get.submitHiveJob(query))
      _   <- ZIO.environmentWithZIO[DPJob](_.get.trackJobProgress(job, trackingInterval))
    } yield job

  def trackJobProgress(job: Job, interval: Duration = Duration.ofSeconds(10)): RIO[DPJob, Unit] =
    ZIO.environmentWithZIO(_.get.trackJobProgress(job, interval))

  def live(cluster: String, project: String, region: String, endpoint: String): TaskLayer[DPJob] =
    ZLayer.scoped(DPJobClient(endpoint).map(dp => DPJobImpl(dp, cluster, project, region)))
}
