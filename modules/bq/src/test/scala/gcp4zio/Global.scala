package gcp4zio

object Global {
  lazy val gcpProject: String = sys.env("GCP_PROJECT")
  lazy val gcsBucket: String  = sys.env("GCS_BUCKET")
}
