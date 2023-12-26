package gcp4zio.dp;

/**
 * Represents the Immutable properties for a cluster instance.
 */
public class InstanceProps {
    private final String machineType;
    private final String bootDiskType;
    private final int bootDiskSizeGb;
    private final int numInstance;

    public InstanceProps(String machineType, String bootDiskType, int bootDiskSizeGb, int numInstance) {
        this.machineType = machineType;
        this.bootDiskType = bootDiskType;
        this.bootDiskSizeGb = bootDiskSizeGb;
        this.numInstance = numInstance;
    }

    public InstanceProps() {
        this("n2-standard-4", "pd-standard", 100, 1);
    }

    public InstanceProps(int numInstance) {
        this("n2-standard-4", "pd-standard", 100, numInstance);
    }

    public String getMachineType() { return machineType; }

    public String getBootDiskType() { return bootDiskType; }

    public int getBootDiskSizeGb() { return bootDiskSizeGb; }

    public int getNumInstance() { return numInstance; }
}