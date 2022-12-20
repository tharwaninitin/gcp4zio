package gcp4zio

import org.slf4j.{Logger, LoggerFactory}

package object bq {
  private[bq] lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  case class BQLoadException(msg: String) extends RuntimeException(msg)

  sealed trait FileType extends Serializable
  object FileType {
    final case class CSV(
        delimiter: String = ",",
        headerPresent: Boolean = true,
        parseMode: String = "FAILFAST",
        quoteChar: String = "\""
    ) extends FileType {
      override def toString: String =
        s"CSV with delimiter => $delimiter header_present => $headerPresent parse_mode => $parseMode"
    }
    final case class JSON(multiLine: Boolean = false) extends FileType {
      override def toString: String = s"Json with multiline  => $multiLine"
    }
    case object BQ      extends FileType
    case object PARQUET extends FileType
    case object ORC     extends FileType
  }
}
