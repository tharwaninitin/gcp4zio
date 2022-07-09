package gcp4zio

object Global {
  lazy val gcsBucket: String = sys.env("GCS_BUCKET")
  val canonicalPath: String  = new java.io.File(".").getCanonicalPath
  val filePathCsv: String    = s"$canonicalPath/src/test/resources/input/ratings.csv"
}
