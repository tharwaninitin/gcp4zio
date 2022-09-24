import com.google.cloud.dataproc.v1.Job
import gcp4zio.dp.{DPJobApi, DPJobLive}
import gcp4zio.gcs.{GCSApi, GCSEnv, GCSLive}
import gcp4zio.utils.ApplicationLogger
import zio.stream.ZPipeline
import zio.{Task, ZIO, ZIOAppDefault}
import java.net.URI

// Before running this application make sure dataproc cluster is running and below mentioned environment variables are set
// export GOOGLE_APPLICATION_CREDENTIALS=
// export GCP_PROJECT=
// export GCP_REGION=
// export DP_CLUSTER=
// export DP_ENDPOINT=

object Main extends ZIOAppDefault with ApplicationLogger {
  val gcpProject: String = sys.env("GCP_PROJECT")
  val gcpRegion: String  = sys.env("GCP_REGION")
  val dpCluster: String  = sys.env("DP_CLUSTER")
  val dpEndpoint: String = sys.env("DP_ENDPOINT")

  def printGcsLogs(response: Job): ZIO[GCSEnv, Throwable, Unit] = {
    val uri    = new URI(response.getDriverOutputResourceUri)
    val bucket = uri.getHost
    val path   = uri.getPath.substring(1)
    GCSApi
      .listObjects(bucket, Some(path), recursive = false, List.empty)
      .flatMap { blob =>
        logger.info(s"Reading logs from gs://$bucket/${blob.getName} with size ${blob.getSize} bytes")
        GCSApi
          .getObject(bucket, blob.getName, 4096)
          .via(ZPipeline.utf8Decode >>> ZPipeline.splitLines)
          .tap(line => ZIO.succeed(logger.info(line)))
      }
      .runDrain
  }

  private val libs = List("file:///usr/lib/spark/examples/jars/spark-examples.jar")
  private val conf = Map("spark.executor.memory" -> "1g", "spark.driver.memory" -> "1g")

  private val mainClass = "org.apache.spark.examples.SparkPi"

  private val program1 = DPJobApi
    .executeSparkJob(List("1000"), mainClass, libs, conf, dpCluster, gcpProject, gcpRegion)
    .flatMap(printGcsLogs)

  private val program2 = for {
    job <- DPJobApi.submitHiveJob("SELE 1 AS ONE", dpCluster, gcpProject, gcpRegion)
    _   <- DPJobApi.trackJobProgress(gcpProject, gcpRegion, job).tapError(_ => printGcsLogs(job))
    _   <- printGcsLogs(job)
  } yield ()

  val run: Task[Unit] = (program1 *> program2).provide(DPJobLive(dpEndpoint) ++ GCSLive())
}
