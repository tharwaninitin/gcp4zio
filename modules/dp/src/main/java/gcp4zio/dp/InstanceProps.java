package gcp4zio.dp;

/**
 * Represents the Immutable properties for a cluster instance.
 */
public record InstanceProps(
    String machineType,
    String bootDiskType,
    int bootDiskSizeGb,
    int numInstance) {

    public InstanceProps() {
        this("n2-standard-4", "pd-standard", 100, 1);
    }

    public InstanceProps(int numInstance) {
        this("n2-standard-4", "pd-standard", 100, numInstance);
    }
}