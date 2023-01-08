package gcp4zio
package dp

import com.google.cloud.dataproc.v1.Cluster
import zio.{Task, TaskLayer, ZIO, ZLayer}

trait DPCluster {
  def createDataproc(cluster: String, props: ClusterProps): Task[Cluster]
  def deleteDataproc(cluster: String): Task[Unit]
}

object DPCluster {
  def createDataproc(cluster: String, props: ClusterProps): ZIO[DPCluster, Throwable, Cluster] =
    ZIO.environmentWithZIO(_.get.createDataproc(cluster, props))

  def deleteDataproc(cluster: String): ZIO[DPCluster, Throwable, Unit] =
    ZIO.environmentWithZIO(_.get.deleteDataproc(cluster))

  def live(project: String, region: String, endpoint: String): TaskLayer[DPCluster] =
    ZLayer.scoped(DPClusterClient(endpoint).map(dp => DPClusterImpl(dp, project, region)))
}
