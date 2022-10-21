package gcp4zio

object Global {
  lazy val gcsBucket: String              = sys.env("GCS_BUCKET")
  val canonicalPath: String               = new java.io.File(".").getCanonicalPath
  val filePathCsv: String                 = s"$canonicalPath/src/test/resources/input/ratings.csv"
  lazy val validTopic: String             = sys.env("VALID_TOPIC_NAME")
  lazy val notValidTopic: String          = sys.env("INVALID_TOPIC_NAME")
  lazy val validNotificationId: String    = sys.env("VALID_NOTIFICATION_ID")
  lazy val notValidNotificationId: String = sys.env("INVALID_NOTIFICATION_ID")
}
