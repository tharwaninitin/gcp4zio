package gcp4zio.dp;

import java.util.List;
import java.util.Optional;

/**
 * Represents the Immutable properties for a Dataproc cluster.
 */
public class ClusterProps {
    private final String bucketName;
    private final boolean singleNode;
    private final boolean internalIpOnly;
    private final Optional<String> subnetUri;
    private final List<String> networkTags;
    private final Optional<String> serviceAccount;
    private final Optional<Integer> idleDeletionDurationSecs;
    private final String imageVersion;
    private final InstanceProps masterInstanceProps;
    private final InstanceProps workerInstanceProps;

    public ClusterProps(String bucketName, boolean singleNode, boolean internalIpOnly, Optional<String> subnetUri,
                        List<String> networkTags, Optional<String> serviceAccount, Optional<Integer> idleDeletionDurationSecs,
                        String imageVersion, InstanceProps masterInstanceProps, InstanceProps workerInstanceProps) {
        this.bucketName = bucketName;
        this.singleNode = singleNode;
        this.internalIpOnly = internalIpOnly;
        this.subnetUri = subnetUri;
        this.networkTags = networkTags;
        this.serviceAccount = serviceAccount;
        this.idleDeletionDurationSecs = idleDeletionDurationSecs;
        this.imageVersion = imageVersion;
        this.masterInstanceProps = masterInstanceProps;
        this.workerInstanceProps = workerInstanceProps;
    }

    public ClusterProps(String bucketName, boolean singleNode, Optional<String> subnetUri, Optional<String> serviceAccount) {
        this(bucketName, singleNode, false, subnetUri, List.of(), serviceAccount, Optional.of(1800),
                "2.1-debian11", new InstanceProps(), new InstanceProps(2));
    }


    public ClusterProps(String bucketName) {
        this(bucketName, false, false, Optional.empty(), List.of(), Optional.empty(), Optional.of(1800),
                "2.1-debian11", new InstanceProps(), new InstanceProps(2));
    }

    public String getBucketName() { return bucketName; }

    public boolean isSingleNode() { return singleNode; }

    public boolean isInternalIpOnly() { return internalIpOnly; }

    public Optional<String> getSubnetUri() { return subnetUri; }

    public List<String> getNetworkTags() { return networkTags; }

    public Optional<String> getServiceAccount() { return serviceAccount; }

    public Optional<Integer> getIdleDeletionDurationSecs() { return idleDeletionDurationSecs; }

    public String getImageVersion() { return imageVersion; }

    public InstanceProps getMasterInstanceProps() { return masterInstanceProps; }

    public InstanceProps getWorkerInstanceProps() { return workerInstanceProps; }
}
