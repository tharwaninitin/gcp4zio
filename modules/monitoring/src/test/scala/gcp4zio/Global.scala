package gcp4zio

object Global {
  lazy val project: String           = sys.env("GCS_PROJECT")
  lazy val metric: String            = sys.env("METRIC")
  lazy val notExistingMetric: String = sys.env("NOT_EXISTING_METRIC")
}
