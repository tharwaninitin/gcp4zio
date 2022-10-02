package gcp4zio
package dp

import com.google.cloud.dataproc.v1.Cluster
import zio.ZIO

trait DPApi[F[_]] {
  def createDataproc(clusterName: String, project: String, region: String, props: ClusterProps): F[Cluster]
  def deleteDataproc(clusterName: String, project: String, region: String): F[Unit]
}

object DPApi {
  def createDataproc(clusterName: String, project: String, region: String, props: ClusterProps): ZIO[DPEnv, Throwable, Cluster] =
    ZIO.environmentWithZIO(_.get.createDataproc(clusterName, project, region, props))
  def deleteDataproc(clusterName: String, project: String, region: String): ZIO[DPEnv, Throwable, Unit] =
    ZIO.environmentWithZIO(_.get.deleteDataproc(clusterName, project, region))
}
