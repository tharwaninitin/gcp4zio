package gcp4zio

object Global {
  lazy val gcsProject: String = sys.env("GCS_PROJECT")
}