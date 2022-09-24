package gcp4zio
package dp

import com.google.cloud.dataproc.v1.Job
import zio.{RIO, ZIO}
import java.time.Duration

trait DPJobApi[F[_]] {
  def submitSparkJob(
      args: List[String],
      mainClass: String,
      libs: List[String],
      conf: Map[String, String],
      cluster: String,
      project: String,
      region: String
  ): F[Job]
  def submitHiveJob(query: String, cluster: String, project: String, region: String): F[Job]
  def trackJobProgress(project: String, region: String, job: Job, interval: Duration): F[Unit]
}

object DPJobApi {
  def submitSparkJob(
      args: List[String],
      mainClass: String,
      libs: List[String],
      conf: Map[String, String],
      cluster: String,
      project: String,
      region: String
  ): RIO[DPJobEnv, Job] =
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
  ): RIO[DPJobEnv, Job] =
    for {
      job <- ZIO.environmentWithZIO[DPJobEnv](_.get.submitSparkJob(args, mainClass, libs, conf, cluster, project, region))
      _   <- ZIO.environmentWithZIO[DPJobEnv](_.get.trackJobProgress(project, region, job, trackingInterval))
    } yield job
  def submitHiveJob(query: String, clusterName: String, project: String, region: String): RIO[DPJobEnv, Job] =
    ZIO.environmentWithZIO(_.get.submitHiveJob(query, clusterName, project, region))
  def trackJobProgress(
      project: String,
      region: String,
      job: Job,
      interval: Duration = Duration.ofSeconds(10)
  ): RIO[DPJobEnv, Unit] =
    ZIO.environmentWithZIO(_.get.trackJobProgress(project, region, job, interval))
}
