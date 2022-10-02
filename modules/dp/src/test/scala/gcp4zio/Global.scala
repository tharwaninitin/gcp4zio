package gcp4zio

object Global {
  lazy val gcpProject: String               = sys.env("GCP_PROJECT")
  lazy val gcpRegion: String                = sys.env("GCP_REGION")
  lazy val dpCluster: String                = sys.env("DP_CLUSTER")
  lazy val dpEndpoint: String               = sys.env("DP_ENDPOINT")
  lazy val dpBucket: String                 = sys.env("DP_BUCKET")
  lazy val dpSubnetUri: Option[String]      = sys.env.get("DP_SUBNET_URI")
  lazy val dpNetworkTags: List[String]      = sys.env.get("DP_NETWORK_TAGS").map(_.split(",").toList).getOrElse(List.empty)
  lazy val dpServiceAccount: Option[String] = sys.env.get("DP_SERVICE_ACCOUNT")
}
