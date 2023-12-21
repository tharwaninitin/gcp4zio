import gcp4zio.batch.Batch
import zio._
import scala.jdk.CollectionConverters._

// export GOOGLE_APPLICATION_CREDENTIALS=
// export GCP_PROJECT=
// export GCP_REGION=

object GCPCloudBatch extends ZIOAppDefault with ApplicationLogger {

  // https://github.com/GoogleCloudPlatform/java-docs-samples/tree/main/batch/snippets/src/main/java
  // https://cloud.google.com/batch/docs/create-run-basic-job

  override val bootstrap: ULayer[Unit] = zioSlf4jLogger

  val gcpProject: String = sys.env("GCP_PROJECT")
  val gcpRegion: String  = sys.env("GCP_REGION")

  val layer: TaskLayer[Batch] = Batch.live(gcpProject, gcpRegion)

  override def run: Task[Unit] = (Batch
    .createJob(
      "example-container-job",
      "gcr.io/google-containers/busybox",
      List("-c", "echo Hello world! This is task ${BATCH_TASK_INDEX}. This job has a total of ${BATCH_TASK_COUNT} tasks."),
      Some("/bin/sh"),
      None
    ) *>
    Batch.listJobs
      .map(_.foreach(job => logger.info(job.getName + " " + job.getStatus.getStatusEventsList.asScala.toList.mkString))))
    .provide(layer)

//    override def run: Task[Unit] = Batch.deleteJob("example-container-job").provide(layer).unit

}
