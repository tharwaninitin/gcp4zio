package gcp4zio

object Global {
  lazy val gcsBucket: String              = sys.env("GCS_BUCKET")
  lazy val canonicalPath: String          = new java.io.File(".").getCanonicalPath
  lazy val filePathCsv: String            = s"$canonicalPath/modules/gcs/src/test/resources/input/ratings.csv"
  lazy val validTopic: String             = sys.env("VALID_TOPIC_NAME")
  lazy val notValidTopic: String          = sys.env("INVALID_TOPIC_NAME")
  lazy val validNotificationId: String    = sys.env("VALID_NOTIFICATION_ID")
  lazy val notValidNotificationId: String = sys.env("INVALID_NOTIFICATION_ID")
}
