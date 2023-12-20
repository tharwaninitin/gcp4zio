package gcp4zio
package dp

import com.google.cloud.dataproc.v1.Cluster
import zio.{Task, TaskLayer, ZIO, ZLayer}

trait DPCluster {

  /** Submits a request to create Dataproc Cluster
    * @param cluster
    *   name of the cluster to be created
    * @param props
    *   cluster props
    * @return
    */
  def createDataproc(cluster: String, props: ClusterProps): Task[Cluster]

  /** Submits a request to delete Dataproc Cluster
    * @param cluster
    *   name of the cluster to be created
    * @return
    */
  def deleteDataproc(cluster: String): Task[Unit]
}

object DPCluster {

  /** Submits a request to create Dataproc Cluster
    *
    * @param cluster
    *   name of the cluster to be created
    * @param props
    *   cluster props
    * @return
    */
  def createDataproc(cluster: String, props: ClusterProps): ZIO[DPCluster, Throwable, Cluster] =
    ZIO.environmentWithZIO(_.get.createDataproc(cluster, props))

  /** Submits a request to delete Dataproc Cluster
    *
    * @param cluster
    *   name of the cluster to be created
    * @return
    */
  def deleteDataproc(cluster: String): ZIO[DPCluster, Throwable, Unit] =
    ZIO.environmentWithZIO(_.get.deleteDataproc(cluster))

  /** Creates live layer required for all [[DPCluster]] API's
    *
    * @param project
    *   GCP projectID
    * @param region
    *   GCP Region name
    * @param endpoint
    *   GCP dataproc API
    * @return
    */
  def live(project: String, region: String, endpoint: String): TaskLayer[DPCluster] =
    ZLayer.scoped(DPClusterClient(endpoint).map(dp => DPClusterImpl(dp, project, region)))
}
