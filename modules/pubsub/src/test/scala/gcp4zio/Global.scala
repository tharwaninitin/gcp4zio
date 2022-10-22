package gcp4zio

object Global {
  lazy val gcsProject: String       = sys.env("GCS_PROJECT")
  lazy val topic: String            = sys.env("TOPIC")
  lazy val nonExistingTopic: String = sys.env("NON_EXISTING_TOPIC")
  lazy val subscription1: String    = sys.env("SUBSCRIPTION_1")
  lazy val subscription2: String    = sys.env("SUBSCRIPTION_2")
}
