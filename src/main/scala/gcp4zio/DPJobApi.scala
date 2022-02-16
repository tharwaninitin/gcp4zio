package gcp4zio

import com.google.cloud.dataproc.v1.Job
import zio.{RIO, Task, ZIO}

object DPJobApi {
  trait Service {
    def executeSparkJob(
        args: List[String],
        mainClass: String,
        libs: List[String],
        conf: Map[String, String],
        clusterName: String,
        project: String,
        region: String
    ): Task[Job]
    def executeHiveJob(query: String, clusterName: String, project: String, region: String): Task[Job]
  }

  def executeSparkJob(
      args: List[String],
      mainClass: String,
      libs: List[String],
      conf: Map[String, String],
      clusterName: String,
      project: String,
      region: String
  ): RIO[DPJobEnv, Job] =
    ZIO.accessM(_.get.executeSparkJob(args, mainClass, libs, conf, clusterName, project, region))
  def executeHiveJob(query: String, clusterName: String, project: String, region: String): RIO[DPJobEnv, Job] =
    ZIO.accessM(_.get.executeHiveJob(query, clusterName, project, region))
}
