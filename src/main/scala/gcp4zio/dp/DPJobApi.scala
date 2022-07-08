package gcp4zio
package dp

import com.google.cloud.dataproc.v1.Job
import zio.{RIO, ZIO}

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
  def trackJob(project: String, region: String, job: Job): F[Unit]
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
  def submitHiveJob(query: String, clusterName: String, project: String, region: String): RIO[DPJobEnv, Job] =
    ZIO.environmentWithZIO(_.get.submitHiveJob(query, clusterName, project, region))
  def trackJob(project: String, region: String, job: Job): RIO[DPJobEnv, Unit] =
    ZIO.environmentWithZIO(_.get.trackJob(project, region, job))
}