import utils.ApplicationLogger
import zio.blocking.Blocking
import zio.stream.{ZSink, ZStream}
import zio.{Has, RIO, Task}
import java.io.IOException

package object gcp4zio extends ApplicationLogger {
  type BlockingTask[A]  = RIO[Blocking, A]
  type GCSStream        = ZStream[Blocking, IOException, Byte]
  type GCSStreamWithEnv = ZStream[GCSEnv with Blocking, IOException, Byte]
  type GCSSink          = ZSink[Blocking, IOException, Byte, Byte, Long]
  type GCSSinkWithEnv   = ZSink[GCSEnv with Blocking, IOException, Byte, Byte, Long]
  type GCSEnv           = Has[GCSApi.Service]
  type BQEnv            = Has[BQApi.Service[Task]]
  type DPEnv            = Has[DPApi.Service]
  type DPJobEnv         = Has[DPJobApi.Service[Task]]

  case class BQLoadException(msg: String) extends RuntimeException(msg)

  case class DataprocProperties(
      bucket_name: String,
      internal_ip_only: Boolean = true,
      subnet_uri: Option[String] = None,
      network_tags: List[String] = List.empty,
      service_account: Option[String] = None,
      idle_deletion_duration_sec: Option[Long] = Some(1800L),
      master_machine_type_uri: String = "n1-standard-4",
      worker_machine_type_uri: String = "n1-standard-4",
      image_version: String = "1.5-debian10",
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
}
