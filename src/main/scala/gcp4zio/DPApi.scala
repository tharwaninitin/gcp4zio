package gcp4zio

import com.google.cloud.dataproc.v1.Cluster
import zio.ZIO
import zio.blocking.Blocking

object DPApi {
  trait Service {
    def createDataproc(clusterName: String, project: String, region: String, config: ClusterProps): BlockingTask[Cluster]
    def deleteDataproc(clusterName: String, project: String, region: String): BlockingTask[Unit]
  }

  def createDataproc(
      clusterName: String,
      project: String,
      region: String,
      config: ClusterProps
  ): ZIO[DPEnv with Blocking, Throwable, Cluster] =
    ZIO.accessM(_.get.createDataproc(clusterName, project, region, config))
  def deleteDataproc(clusterName: String, project: String, region: String): ZIO[DPEnv with Blocking, Throwable, Unit] =
    ZIO.accessM(_.get.deleteDataproc(clusterName, project, region))
}
