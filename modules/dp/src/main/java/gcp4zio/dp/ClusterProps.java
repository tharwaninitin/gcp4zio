package gcp4zio.dp;

import java.util.Optional;

/**
 * Represents the Immutable properties for a Dataproc cluster.
 */
public record ClusterProps(
    String bucketName,
    boolean singleNode,
    String imageVersion,
    Optional<Integer> idleDeletionDurationSecs,
    GCEClusterProps gceClusterProps,
    InstanceProps masterInstanceProps,
    InstanceProps workerInstanceProps) {

    public ClusterProps(String bucketName, boolean singleNode, Optional<String> subnetUri, Optional<String> serviceAccount) {
        this(bucketName, singleNode, "2.1-debian11", Optional.of(1800), new GCEClusterProps(false, subnetUri, serviceAccount), new InstanceProps(), new InstanceProps(2));
    }

    public ClusterProps(String bucketName) {
        this(bucketName, true, "2.1-debian11", Optional.of(1800), new GCEClusterProps(false), new InstanceProps(), new InstanceProps(2));

    }
}
