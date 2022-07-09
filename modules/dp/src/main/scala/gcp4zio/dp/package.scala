package gcp4zio

import gcp4zio.utils.ApplicationLogger
import zio.Task

package object dp extends ApplicationLogger {
  type DPEnv    = DPApi[Task]
  type DPJobEnv = DPJobApi[Task]

  case class ClusterProps(
      bucketName: String,
      internalIpOnly: Boolean = true,
      subnetUri: Option[String] = None,
      networkTags: List[String] = List.empty,
      serviceAccount: Option[String] = None,
      idleDeletionDurationSecs: Option[Long] = Some(1800L),
      masterMachineType: String = "n1-standard-4",
      workerMachineType: String = "n1-standard-4",
      imageVersion: String = "1.5-debian10",
      bootDiskType: String = "pd-ssd",
      masterBootDiskSizeGb: Int = 400,
      workerBootDiskSizeGb: Int = 200,
      masterNumInstance: Int = 1,
      workerNumInstance: Int = 3
  )
}
