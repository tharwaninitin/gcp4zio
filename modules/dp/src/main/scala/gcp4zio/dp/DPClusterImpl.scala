package gcp4zio
package dp

import com.google.cloud.dataproc.v1._
import com.google.protobuf.Duration
import zio.{Task, ZIO}
import scala.jdk.CollectionConverters._

case class DPClusterImpl(client: ClusterControllerClient, project: String, region: String) extends DPCluster {

  private def createClusterConfig(props: ClusterProps): ClusterConfig = {
    val endPointConfig = EndpointConfig.newBuilder().setEnableHttpPortAccess(true)
    val softwareConfig = SoftwareConfig.newBuilder().setImageVersion(props.imageVersion)

    val diskConfigM = DiskConfig.newBuilder().setBootDiskType(props.bootDiskType).setBootDiskSizeGb(props.masterBootDiskSizeGb)
    val diskConfigW = DiskConfig.newBuilder().setBootDiskType(props.bootDiskType).setBootDiskSizeGb(props.workerBootDiskSizeGb)

    val gceClusterBuilder: GceClusterConfig.Builder = props.subnetUri match {
      case Some(value) =>
        GceClusterConfig.newBuilder().setInternalIpOnly(props.internalIpOnly).setSubnetworkUri(value).addAllTags(props.networkTags.asJava)
      case None =>
        GceClusterConfig.newBuilder().setInternalIpOnly(props.internalIpOnly).addAllTags(props.networkTags.asJava)
    }

    val gceClusterConfig: GceClusterConfig.Builder = props.serviceAccount match {
      case Some(value) => gceClusterBuilder.setServiceAccount(value)
      case _           => gceClusterBuilder.addServiceAccountScopes("https://www.googleapis.com/auth/cloud-platform")
    }

    val masterConfig = InstanceGroupConfig.newBuilder
      .setMachineTypeUri(props.masterMachineType)
      .setNumInstances(props.masterNumInstance)
      .setDiskConfig(diskConfigM)
      .build

    val workerConfig = InstanceGroupConfig.newBuilder
      .setMachineTypeUri(props.workerMachineType)
      .setNumInstances(props.workerNumInstance)
      .setDiskConfig(diskConfigW)
      .build

    val clusterConfigBuilder = ClusterConfig.newBuilder
      .setMasterConfig(masterConfig)
      .setWorkerConfig(workerConfig)
      .setSoftwareConfig(softwareConfig)
      .setConfigBucket(props.bucketName)
      .setGceClusterConfig(gceClusterConfig)
      .setEndpointConfig(endPointConfig)

    props.idleDeletionDurationSecs match {
      case Some(value) =>
        clusterConfigBuilder
          .setLifecycleConfig(LifecycleConfig.newBuilder().setIdleDeleteTtl(Duration.newBuilder().setSeconds(value)))
          .build
      case _ => clusterConfigBuilder.build
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
