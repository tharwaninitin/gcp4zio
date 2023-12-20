package gcp4zio

import org.slf4j.{Logger, LoggerFactory}

package object dp {
  private[dp] lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  case class InstanceProps(
      machineType: String = "n2-standard-4",
      bootDiskType: String = "pd-standard", // pd-standard, pd-balanced, pd-ssd // https://cloud.google.com/compute/docs/disks#disk-types
      bootDiskSizeGb: Int = 100,
      numInstance: Int = 1
  )

  case class ClusterProps(
      bucketName: String,
      singleNode: Boolean = false,
      internalIpOnly: Boolean = false,
      subnetUri: Option[String] = None,
      networkTags: List[String] = List.empty,
      serviceAccount: Option[String] = None,
      idleDeletionDurationSecs: Option[Long] = Some(1800L),
      imageVersion: String = "2.1-debian11",
      masterInstanceProps: InstanceProps = InstanceProps(),
      workerInstanceProps: InstanceProps = InstanceProps(numInstance = 2)
  )
}
