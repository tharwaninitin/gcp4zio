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
    ZLayer.scoped(
      DPClusterClient(endpoint).map(client =>
        new DPCluster {

          val dpClusterImpl = new DPClusterImpl(client, project, region)

          override def createDataproc(cluster: String, props: ClusterProps): Task[Cluster] = ZIO
            .fromFutureJava(dpClusterImpl.createDataproc(cluster, props))
            .tapBoth(
              e => ZIO.succeed(logger.error(s"Cluster creation failed with error ${e.getMessage}")),
              res => ZIO.succeed(logger.info(s"Cluster ${res.getClusterName} created successfully"))
            )

          override def deleteDataproc(cluster: String): Task[Unit] = ZIO
            .fromFutureJava {
              logger.info(s"Submitting cluster deletion request for $cluster")
              client.deleteClusterAsync(project, region, cluster)
            }
            .tapBoth(
              e => ZIO.succeed(logger.error(s"Cluster deletion failed with error ${e.getMessage}")),
              _ => ZIO.succeed(logger.info(s"Cluster $cluster deleted successfully"))
            )
            .unit
        }
      )
    )
}
