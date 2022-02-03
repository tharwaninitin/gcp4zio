package gcp4zio

import com.google.cloud.dataproc.v1._
import com.google.protobuf.Duration
import zio.{Managed, TaskLayer, UIO, ZIO}
import scala.jdk.CollectionConverters._

case class DP(client: ClusterControllerClient) extends DPApi.Service {

  def createDataproc(clusterName: String, project: String, region: String, props: DataprocProperties): BlockingTask[Cluster] = ZIO
    .fromFutureJava {
      val end_point_config = EndpointConfig.newBuilder().setEnableHttpPortAccess(true)
      val software_config  = SoftwareConfig.newBuilder().setImageVersion(props.image_version)
      val disk_config_m =
        DiskConfig
          .newBuilder()
          .setBootDiskType(props.boot_disk_type)
          .setBootDiskSizeGb(props.master_boot_disk_size_gb)
      val disk_config_w =
        DiskConfig
          .newBuilder()
          .setBootDiskType(props.boot_disk_type)
          .setBootDiskSizeGb(props.worker_boot_disk_size_gb)

      val gce_cluster_builder = props.subnet_uri match {
        case Some(value) =>
          GceClusterConfig
            .newBuilder()
            .setInternalIpOnly(props.internal_ip_only)
            .setSubnetworkUri(value)
            .addAllTags(props.network_tags.asJava)
            .addServiceAccountScopes("https://www.googleapis.com/auth/cloud-platform")
        case None =>
          GceClusterConfig
            .newBuilder()
            .setInternalIpOnly(props.internal_ip_only)
            .addAllTags(props.network_tags.asJava)
            .addServiceAccountScopes("https://www.googleapis.com/auth/cloud-platform")
      }

      val gce_cluster_config = props.service_account match {
        case Some(value) => gce_cluster_builder.setServiceAccount(value)
        case _           => gce_cluster_builder
      }

      val master_config = InstanceGroupConfig.newBuilder
        .setMachineTypeUri(props.master_machine_type_uri)
        .setNumInstances(props.master_num_instance)
        .setDiskConfig(disk_config_m)
        .build
      val worker_config = InstanceGroupConfig.newBuilder
        .setMachineTypeUri(props.worker_machine_type_uri)
        .setNumInstances(props.worker_num_instance)
        .setDiskConfig(disk_config_w)
        .build
      val cluster_config_builder = ClusterConfig.newBuilder
        .setMasterConfig(master_config)
        .setWorkerConfig(worker_config)
        .setSoftwareConfig(software_config)
        .setConfigBucket(props.bucket_name)
        .setGceClusterConfig(gce_cluster_config)
        .setEndpointConfig(end_point_config)

      val cluster_config = props.idle_deletion_duration_sec match {
        case Some(value) =>
          cluster_config_builder
            .setLifecycleConfig(
              LifecycleConfig.newBuilder().setIdleDeleteTtl(Duration.newBuilder().setSeconds(value))
            )
            .build
        case _ => cluster_config_builder.build
      }

      val cluster = Cluster.newBuilder.setClusterName(clusterName).setConfig(cluster_config).build

      val create_cluster_async_request = client.createClusterAsync(project, region, cluster)

      logger.info(s"Submitting cluster creation request for $clusterName")
      create_cluster_async_request
    }
    .tapBoth(
      e => UIO(logger.error(s"Cluster creation failed with error ${e.getMessage}")),
      res => UIO(logger.info(s"Cluster ${res.getClusterName} created successfully"))
    )

  def deleteDataproc(clusterName: String, project: String, region: String): BlockingTask[Unit] = ZIO
    .fromFutureJava {
      logger.info(s"Submitting cluster deletion request for $clusterName")
      client.deleteClusterAsync(project, region, clusterName)
    }
    .tapBoth(
      e => UIO(logger.error(s"Cluster deletion failed with error ${e.getMessage}")),
      _ => UIO(logger.info(s"Cluster $clusterName deleted successfully"))
    )
    .unit
}

object DP {
  def live(endpoint: String): TaskLayer[DPEnv] = Managed.fromAutoCloseable(DPClient(endpoint)).map(dp => DP(dp)).toLayer
}
