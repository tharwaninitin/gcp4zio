import utils.ApplicationLogger
import zio.{Has, Task}

package object gcp4zio extends ApplicationLogger {

  type GCSEnv   = Has[GCSApi.Service[Task]]
  type BQEnv    = Has[BQApi.Service[Task]]
  type DPEnv    = Has[DPApi.Service[Task]]
  type DPJobEnv = Has[DPJobApi.Service[Task]]

  case class BQLoadException(msg: String) extends RuntimeException(msg)

  case class DataprocProperties(
      bucket_name: String,
      subnet_uri: Option[String] = None,
      network_tags: List[String] = List.empty,
      service_account: Option[String] = None,
      idle_deletion_duration_sec: Option[Long] = Some(1800L),
      master_machine_type_uri: String = "n1-standard-4",
      worker_machine_type_uri: String = "n1-standard-4",
      image_version: String = "1.5.4-debian10",
      boot_disk_type: String = "pd-ssd",
      master_boot_disk_size_gb: Int = 400,
      worker_boot_disk_size_gb: Int = 200,
      master_num_instance: Int = 1,
      worker_num_instance: Int = 3
  )

  sealed trait BQInputType extends Serializable
  object BQInputType {
    case class CSV(
        delimiter: String = ",",
        header_present: Boolean = true,
        parse_mode: String = "FAILFAST",
        quotechar: String = "\""
    ) extends BQInputType {
      override def toString: String =
        s"CSV with delimiter => $delimiter header_present => $header_present parse_mode => $parse_mode"
    }
    case class JSON(multi_line: Boolean = false) extends BQInputType {
      override def toString: String = s"Json with multiline  => $multi_line"
    }
    case object BQ      extends BQInputType
    case object PARQUET extends BQInputType
    case object ORC     extends BQInputType
  }

  sealed trait FSType
  object FSType {
    case object LOCAL extends FSType
    case object GCS   extends FSType
  }

  sealed trait Location
  object Location {
    case class LOCAL(path: String)               extends Location
    case class GCS(bucket: String, path: String) extends Location
  }
}
