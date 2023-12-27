package gcp4zio.dp;

import java.util.List;
import java.util.Optional;

/**
 * Represents the Immutable properties for a Dataproc cluster.
 */
public record GCEClusterProps(
    boolean internalIpOnly,
    Optional<String> subnetUri,
    List<String> networkTags,
    Optional<String> serviceAccount) {

    public GCEClusterProps(boolean internalIpOnly) {
        this(internalIpOnly, Optional.empty(), List.of(), Optional.empty());
    }

    public GCEClusterProps(boolean internalIpOnly, Optional<String> subnetUri, Optional<String> serviceAccount) {
        this(internalIpOnly, subnetUri, List.of(), serviceAccount);
    }
}
