package gcp4zio.dp;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.dataproc.v1.*;
import com.google.protobuf.Duration;
import com.google.protobuf.Empty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DPClusterImpl {

  private static final Logger logger = LoggerFactory.getLogger(DPClusterImpl.class);

  private final ClusterControllerClient client;
  private final String project;
  private final String region;

  public DPClusterImpl(ClusterControllerClient client, String project, String region) {
    this.client = client;
    this.project = project;
    this.region = region;
  }

  /**
   * This function creates Dataproc Software Config - Image Version, Single Node
   *
   * @param props ClusterProps
   * @return SoftwareConfig
   */
  private SoftwareConfig createSoftwareConfig(ClusterProps props) {
    if (props.singleNode()) {
      return SoftwareConfig.newBuilder()
          .setImageVersion(props.imageVersion())
          .putProperties("dataproc:dataproc.allow.zero.workers", "true")
          .build();
    } else {
      return SoftwareConfig.newBuilder().setImageVersion(props.imageVersion()).build();
    }
  }

  /**
   * This function creates GCE Cluster Config - GCE Network, GCE Service Accounts, GCE Scopes
   *
   * @param props GCEProps
   * @return GceClusterConfig
   */
  private GceClusterConfig createGCEClusterConfig(GCEProps props) {
    GceClusterConfig.Builder gceClusterBuilder =
        GceClusterConfig.newBuilder().setInternalIpOnly(props.internalIpOnly());

    if (props.subnetUri().isPresent()) {
      gceClusterBuilder.setSubnetworkUri(props.subnetUri().get()).addAllTags(props.networkTags());
    } else {
      gceClusterBuilder.addAllTags(props.networkTags());
    }

    if (props.serviceAccount().isPresent()) {
      return gceClusterBuilder.setServiceAccount(props.serviceAccount().get()).build();
    } else {
      return gceClusterBuilder
          .addServiceAccountScopes("https://www.googleapis.com/auth/cloud-platform")
          .build();
    }
  }

  /**
   * This function creates GCE Instance Config - Instance Disk, Machine Type, Number of Nodes
   *
   * @param props ClusterProps
   * @return InstanceGroupConfig
   */
  private InstanceGroupConfig createGCEInstanceConfig(InstanceProps props) {
    DiskConfig diskConfig =
        DiskConfig.newBuilder()
            .setBootDiskType(props.bootDiskType())
            .setBootDiskSizeGb(props.bootDiskSizeGb())
            .build();
    return InstanceGroupConfig.newBuilder()
        .setMachineTypeUri(props.machineType())
        .setNumInstances(props.numInstance())
        .setDiskConfig(diskConfig)
        .build();
  }

  /**
   * This function creates Dataproc Cluster Config combining all configs
   *
   * @param props ClusterProps
   * @return ClusterConfig
   */
  private ClusterConfig createClusterConfig(ClusterProps props) {
    if (props.singleNode()) {
      logger.info("Creating single node cluster creation request");
    } else {
      logger.info("Creating multi node cluster creation request");
    }

    SoftwareConfig softwareConfig = createSoftwareConfig(props);
    GceClusterConfig gceClusterConfig = createGCEClusterConfig(props.gceClusterProps());
    InstanceGroupConfig masterConfig = createGCEInstanceConfig(props.masterInstanceProps());
    EndpointConfig endPointConfig =
        EndpointConfig.newBuilder().setEnableHttpPortAccess(true).build();

    ClusterConfig.Builder clusterConfigBuilder =
        ClusterConfig.newBuilder()
            .setMasterConfig(masterConfig)
            .setSoftwareConfig(softwareConfig)
            .setConfigBucket(props.bucketName())
            .setGceClusterConfig(gceClusterConfig)
            .setEndpointConfig(endPointConfig);

    if (!props.singleNode()) {
      InstanceGroupConfig workerConfig = createGCEInstanceConfig(props.workerInstanceProps());
      clusterConfigBuilder.setWorkerConfig(workerConfig);
    }

    if (props.idleDeletionDurationSecs().isPresent()) {
      return clusterConfigBuilder
          .setLifecycleConfig(
              LifecycleConfig.newBuilder()
                  .setIdleDeleteTtl(
                      Duration.newBuilder().setSeconds(props.idleDeletionDurationSecs().get())))
          .build();
    } else {
      return clusterConfigBuilder.build();
    }
  }

  /**
   * This public function creates Dataproc Cluster
   *
   * @param cluster String
   * @param props ClusterProps
   * @return OperationFuture<Cluster, ClusterOperationMetadata>
   */
  public OperationFuture<Cluster, ClusterOperationMetadata> createDataproc(
      String cluster, ClusterProps props) {
    ClusterConfig clusterConfig = createClusterConfig(props);

    Cluster clusterConf =
        Cluster.newBuilder().setClusterName(cluster).setConfig(clusterConfig).build();

    logger.info("Submitting cluster creation request for {}", cluster);
    return client.createClusterAsync(project, region, clusterConf);
  }

  /**
   * This public function deletes Dataproc Cluster
   *
   * @param cluster String
   * @return OperationFuture<Cluster, ClusterOperationMetadata>
   */
  public OperationFuture<Empty, ClusterOperationMetadata> deleteDataproc(String cluster) {
    logger.info("Submitting cluster deletion request for {}", cluster);
    return client.deleteClusterAsync(project, region, cluster);
  }
}
