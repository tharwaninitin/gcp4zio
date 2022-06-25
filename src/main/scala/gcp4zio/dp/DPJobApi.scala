package gcp4zio
package dp

import com.google.cloud.dataproc.v1.Job
import zio.{RIO, ZIO}

trait DPJobApi[F[_]] {
  def executeSparkJob(
      args: List[String],
      mainClass: String,
      libs: List[String],
      conf: Map[String, String],
      clusterName: String,
      project: String,
      region: String
  ): F[Job]
  def executeHiveJob(query: String, clusterName: String, project: String, region: String): F[Job]
}

object DPJobApi {
  def executeSparkJob(
      args: List[String],
      mainClass: String,
      libs: List[String],
      conf: Map[String, String],
      clusterName: String,
      project: String,
      region: String
  ): RIO[DPJobEnv, Job] =
    ZIO.environmentWithZIO(_.get.executeSparkJob(args, mainClass, libs, conf, clusterName, project, region))
  def executeHiveJob(query: String, clusterName: String, project: String, region: String): RIO[DPJobEnv, Job] =
    ZIO.environmentWithZIO(_.get.executeHiveJob(query, clusterName, project, region))
}
