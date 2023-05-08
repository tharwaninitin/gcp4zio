package gcp4zio

object Global {
  lazy val gcsProject: String       = sys.env("GCS_PROJECT")
  lazy val topic1: String           = sys.env("TOPIC")
  lazy val nonExistingTopic: String = sys.env("NON_EXISTING_TOPIC")
  lazy val subscription1: String    = sys.env("SUBSCRIPTION")
  lazy val subscription2: String    = sys.env("SUBSCRIPTION_2")
  lazy val member: String           = sys.env("MEMBER")
  lazy val role: String             = sys.env("ROLE")
}
