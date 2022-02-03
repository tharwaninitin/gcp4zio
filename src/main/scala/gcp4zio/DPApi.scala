package gcp4zio

import com.google.cloud.dataproc.v1.Cluster
import zio.ZIO

object DPApi {
  trait Service[F[_]] {
    def createDataproc(clusterName: String, project: String, region: String, props: DataprocProperties): F[Cluster]
    def deleteDataproc(clusterName: String, project: String, region: String): F[Unit]
  }

  def createDataproc(
      clusterName: String,
      project: String,
      region: String,
      props: DataprocProperties
  ): ZIO[DPEnv, Throwable, Cluster] =
    ZIO.accessM(_.get.createDataproc(clusterName, project, region, props))
  def deleteDataproc(clusterName: String, project: String, region: String): ZIO[DPEnv, Throwable, Unit] =
    ZIO.accessM(_.get.deleteDataproc(clusterName, project, region))
}
