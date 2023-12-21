package gcp4zio
package dp

import com.google.cloud.dataproc.v1._
import com.google.protobuf.Duration
import zio.{Task, ZIO}
import scala.jdk.CollectionConverters._

case class DPClusterImpl(client: ClusterControllerClient, project: String, region: String) extends DPCluster {

  /** This function creates Dataproc Software Config - Image Version, Single Node
    *
    * @param props
    *   ClusterProps
    * @return
    */
  private def createSoftwareConfig(props: ClusterProps): SoftwareConfig =
    if (props.singleNode) {
      SoftwareConfig.newBuilder().setImageVersion(props.imageVersion).putProperties("dataproc:dataproc.allow.zero.workers", "true").build()
    } else {
      SoftwareConfig.newBuilder().setImageVersion(props.imageVersion).build()
    }

  /** This function creates GCE Cluster Config - GCE Network, GCE Service Accounts, GCE Scopes
    *
    * @param props
    *   ClusterProps
    * @return
    */
  private def createGCEClusterConfig(props: ClusterProps): GceClusterConfig = {
    val gceClusterBuilder: GceClusterConfig.Builder = props.subnetUri match {
      case Some(value) =>
        GceClusterConfig
          .newBuilder()
          .setInternalIpOnly(props.internalIpOnly)
          .setSubnetworkUri(value)
          .addAllTags(props.networkTags.asJava)
      case None =>
        GceClusterConfig.newBuilder().setInternalIpOnly(props.internalIpOnly).addAllTags(props.networkTags.asJava)
    }

    props.serviceAccount match {
      case Some(value) => gceClusterBuilder.setServiceAccount(value).build()
      case _           => gceClusterBuilder.addServiceAccountScopes("https://www.googleapis.com/auth/cloud-platform").build()
    }
  }

  /** This function creates GCE Instance Config - Instance Disk, Machine Type, Number of Nodes
    *
    * @param props
    *   ClusterProps
    * @return
    */
  private def createGCEInstanceConfig(props: InstanceProps): InstanceGroupConfig = {
    val diskConfigM = DiskConfig
      .newBuilder()
      .setBootDiskType(props.bootDiskType)
      .setBootDiskSizeGb(props.bootDiskSizeGb)
      .build()
    InstanceGroupConfig
      .newBuilder()
      .setMachineTypeUri(props.machineType)
      .setNumInstances(props.numInstance)
      .setDiskConfig(diskConfigM)
      .build()
  }

  /** This function creates Dataproc Cluster Config combining all configs
    *
    * @param props
    *   ClusterProps
    * @return
    */
  private def createClusterConfig(props: ClusterProps): ClusterConfig = {
    if (props.singleNode)
      logger.info(s"Creating single node cluster creation request")
    else
      logger.info(s"Creating multi node cluster creation request")

    val softwareConfig = createSoftwareConfig(props)

    val gceClusterConfig = createGCEClusterConfig(props)

    val masterConfig = createGCEInstanceConfig(props.masterInstanceProps)

    val endPointConfig = EndpointConfig.newBuilder().setEnableHttpPortAccess(true).build()

    val clusterConfigBuilder =
      if (props.singleNode)
        ClusterConfig.newBuilder
          .setMasterConfig(masterConfig)
          .setSoftwareConfig(softwareConfig)
          .setConfigBucket(props.bucketName)
          .setGceClusterConfig(gceClusterConfig)
          .setEndpointConfig(endPointConfig)
      else {
        val workerConfig = createGCEInstanceConfig(props.workerInstanceProps)
        ClusterConfig.newBuilder
          .setMasterConfig(masterConfig)
          .setWorkerConfig(workerConfig)
          .setSoftwareConfig(softwareConfig)
          .setConfigBucket(props.bucketName)
          .setGceClusterConfig(gceClusterConfig)
          .setEndpointConfig(endPointConfig)
      }

    props.idleDeletionDurationSecs match {
      case Some(value) =>
        clusterConfigBuilder
          .setLifecycleConfig(LifecycleConfig.newBuilder().setIdleDeleteTtl(Duration.newBuilder().setSeconds(value)))
          .build()
      case _ => clusterConfigBuilder.build()
    }
  }

  def createDataproc(clusterName: String, props: ClusterProps): Task[Cluster] = ZIO
    .fromFutureJava {
      val clusterConfig = createClusterConfig(props)

      val cluster = Cluster.newBuilder.setClusterName(clusterName).setConfig(clusterConfig).build

      val createClusterAsyncRequest = client.createClusterAsync(project, region, cluster)

      logger.info(s"Submitting cluster creation request for $clusterName")
      createClusterAsyncRequest
    }
    .tapBoth(
      e => ZIO.succeed(logger.error(s"Cluster creation failed with error ${e.getMessage}")),
      res => ZIO.succeed(logger.info(s"Cluster ${res.getClusterName} created successfully"))
    )

  def deleteDataproc(clusterName: String): Task[Unit] = ZIO
    .fromFutureJava {
      logger.info(s"Submitting cluster deletion request for $clusterName")
      client.deleteClusterAsync(project, region, clusterName)
    }
    .tapBoth(
      e => ZIO.succeed(logger.error(s"Cluster deletion failed with error ${e.getMessage}")),
      _ => ZIO.succeed(logger.info(s"Cluster $clusterName deleted successfully"))
    )
    .unit
}
