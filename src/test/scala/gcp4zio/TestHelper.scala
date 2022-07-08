package gcp4zio

import org.slf4j.{Logger, LoggerFactory}

trait TestHelper {
  lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  lazy val gcpProject: String               = sys.env("GCP_PROJECT")
  lazy val gcpRegion: String                = sys.env("GCP_REGION")
  lazy val dpCluster: String                = sys.env("DP_CLUSTER")
  lazy val dpEndpoint: String               = sys.env("DP_ENDPOINT")
  lazy val dpBucket: String                 = sys.env("DP_BUCKET")
  lazy val gcsBucket: String                = sys.env("GCS_BUCKET")
  lazy val gcsInputLocation: String         = sys.env("GCS_INPUT_LOCATION")
  lazy val gcsOutputLocation: String        = sys.env("GCS_OUTPUT_LOCATION")
  lazy val dpSubnetUri: Option[String]      = sys.env.get("DP_SUBNET_URI")
  lazy val dpNetworkTags: List[String]      = sys.env.get("DP_NETWORK_TAGS").map(_.split(",").toList).getOrElse(List.empty)
  lazy val dpServiceAccount: Option[String] = sys.env.get("DP_SERVICE_ACCOUNT")

  val canonicalPath: String   = new java.io.File(".").getCanonicalPath
  val filePathParquet: String = s"$canonicalPath/src/test/resources/input/ratings.parquet"
  val filePathCsv: String     = s"$canonicalPath/src/test/resources/input/ratings.csv"
}
