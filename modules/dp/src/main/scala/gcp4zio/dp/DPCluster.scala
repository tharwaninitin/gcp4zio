package gcp4zio
package dp

import com.google.cloud.dataproc.v1.Cluster
import zio.{Task, TaskLayer, ZIO, ZLayer}

trait DPCluster {
  def createDataproc(cluster: String, project: String, region: String, props: ClusterProps): Task[Cluster]
  def deleteDataproc(cluster: String, project: String, region: String): Task[Unit]
}

object DPCluster {
  def createDataproc(cluster: String, project: String, region: String, props: ClusterProps): ZIO[DPCluster, Throwable, Cluster] =
    ZIO.environmentWithZIO(_.get.createDataproc(cluster, project, region, props))
  def deleteDataproc(cluster: String, project: String, region: String): ZIO[DPCluster, Throwable, Unit] =
    ZIO.environmentWithZIO(_.get.deleteDataproc(cluster, project, region))
  def live(endpoint: String): TaskLayer[DPCluster] = ZLayer.scoped(DPClusterClient(endpoint).map(dp => DPClusterImpl(dp)))
}
