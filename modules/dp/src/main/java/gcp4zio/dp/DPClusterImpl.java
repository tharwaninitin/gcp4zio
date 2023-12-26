package gcp4zio.dp;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.dataproc.v1.*;
import com.google.protobuf.Duration;

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

    /** This function creates Dataproc Software Config - Image Version, Single Node
     *
     * @param props ClusterProps
     * @return SoftwareConfig
     */
    private SoftwareConfig createSoftwareConfig(ClusterProps props) {
        if (props.isSingleNode()) {
            return SoftwareConfig.newBuilder()
                    .setImageVersion(props.getImageVersion())
                    .putProperties("dataproc:dataproc.allow.zero.workers", "true")
                    .build();
        } else {
            return SoftwareConfig.newBuilder().setImageVersion(props.getImageVersion()).build();
        }
    }

    /** This function creates GCE Cluster Config - GCE Network, GCE Service Accounts, GCE Scopes
     *
     * @param props ClusterProps
     * @return GceClusterConfig
     */
    private GceClusterConfig createGCEClusterConfig(ClusterProps props) {
        GceClusterConfig.Builder gceClusterBuilder = GceClusterConfig.newBuilder()
                .setInternalIpOnly(props.isInternalIpOnly());

        if (props.getSubnetUri().isPresent()) {
            gceClusterBuilder.setSubnetworkUri(props.getSubnetUri().get())
                    .addAllTags(props.getNetworkTags());
        } else {
            gceClusterBuilder.addAllTags(props.getNetworkTags());
        }

        if (props.getServiceAccount().isPresent()) {
            return gceClusterBuilder.setServiceAccount(props.getServiceAccount().get()).build();
        } else {
            return gceClusterBuilder.addServiceAccountScopes("https://www.googleapis.com/auth/cloud-platform").build();
        }
    }

    /** This function creates GCE Instance Config - Instance Disk, Machine Type, Number of Nodes
     *
     * @param props ClusterProps
     * @return InstanceGroupConfig
     */
    private InstanceGroupConfig createGCEInstanceConfig(InstanceProps props) {
        DiskConfig diskConfig = DiskConfig.newBuilder()
                .setBootDiskType(props.getBootDiskType())
                .setBootDiskSizeGb(props.getBootDiskSizeGb())
                .build();
        return InstanceGroupConfig.newBuilder()
                .setMachineTypeUri(props.getMachineType())
                .setNumInstances(props.getNumInstance())
                .setDiskConfig(diskConfig)
                .build();
    }

    /** This function creates Dataproc Cluster Config combining all configs
     *
     * @param props ClusterProps
     * @return ClusterConfig
     */
    private ClusterConfig createClusterConfig(ClusterProps props) {
        if (props.isSingleNode()) {
            logger.info("Creating single node cluster creation request");
        } else {
            logger.info("Creating multi node cluster creation request");
        }

        SoftwareConfig softwareConfig = createSoftwareConfig(props);
        GceClusterConfig gceClusterConfig = createGCEClusterConfig(props);
        InstanceGroupConfig masterConfig = createGCEInstanceConfig(props.getMasterInstanceProps());
        EndpointConfig endPointConfig = EndpointConfig.newBuilder().setEnableHttpPortAccess(true).build();

        ClusterConfig.Builder clusterConfigBuilder = ClusterConfig.newBuilder()
                .setMasterConfig(masterConfig)
                .setSoftwareConfig(softwareConfig)
                .setConfigBucket(props.getBucketName())
                .setGceClusterConfig(gceClusterConfig)
                .setEndpointConfig(endPointConfig);

        if (!props.isSingleNode()) {
            InstanceGroupConfig workerConfig = createGCEInstanceConfig(props.getWorkerInstanceProps());
            clusterConfigBuilder.setWorkerConfig(workerConfig);
        }

        if (props.getIdleDeletionDurationSecs().isPresent()) {
            return clusterConfigBuilder
                    .setLifecycleConfig(LifecycleConfig.newBuilder().setIdleDeleteTtl(Duration.newBuilder().setSeconds(props.getIdleDeletionDurationSecs().get())))
                    .build();
        } else {
            return clusterConfigBuilder.build();
        }
    }


    /** This public function creates Dataproc Cluster
     *
     * @param cluster String
     * @param props ClusterProps
     * @return OperationFuture<Cluster, ClusterOperationMetadata>
     */
    public OperationFuture<Cluster, ClusterOperationMetadata> createDataproc(String cluster, ClusterProps props) {
        ClusterConfig clusterConfig = createClusterConfig(props);

        Cluster clusterConf = Cluster.newBuilder()
                .setClusterName(cluster)
                .setConfig(clusterConfig)
                .build();

        logger.info("Submitting cluster creation request for {}", cluster);
        return client.createClusterAsync(project, region, clusterConf);
    }

}
