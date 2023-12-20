package gcp4zio

import org.slf4j.{Logger, LoggerFactory}

package object dp {
  private[dp] lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  case class ClusterProps(
      bucketName: String,
      internalIpOnly: Boolean = true,
      subnetUri: Option[String] = None,
      networkTags: List[String] = List.empty,
      serviceAccount: Option[String] = None,
      idleDeletionDurationSecs: Option[Long] = Some(1800L),
      masterMachineType: String = "n1-standard-4",
      workerMachineType: String = "n1-standard-4",
      imageVersion: String = "2.1-debian11",
      bootDiskType: String = "pd-standard", // pd-standard, pd-balanced, pd-ssd // https://cloud.google.com/compute/docs/disks#disk-types
      masterBootDiskSizeGb: Int = 100,
      workerBootDiskSizeGb: Int = 100,
      masterNumInstance: Int = 1,
      workerNumInstance: Int = 3
  )
}
