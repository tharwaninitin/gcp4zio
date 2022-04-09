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
  type DPJobEnv         = Has[DPJobApi.Service]

  case class BQLoadException(msg: String) extends RuntimeException(msg)

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

  sealed trait BQInputType extends Serializable
  object BQInputType {
    final case class CSV(
        delimiter: String = ",",
        headerPresent: Boolean = true,
        parseMode: String = "FAILFAST",
        quoteChar: String = "\""
    ) extends BQInputType {
      override def toString: String =
        s"CSV with delimiter => $delimiter header_present => $headerPresent parse_mode => $parseMode"
    }
    final case class JSON(multiLine: Boolean = false) extends BQInputType {
      override def toString: String = s"Json with multiline  => $multiLine"
    }
    case object BQ      extends BQInputType
    case object PARQUET extends BQInputType
    case object ORC     extends BQInputType
  }
}
