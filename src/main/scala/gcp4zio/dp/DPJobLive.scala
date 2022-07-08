package gcp4zio
package dp

import com.google.cloud.dataproc.v1._
import zio.{Task, TaskLayer, ZIO, ZLayer}
import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters._

@SuppressWarnings(Array("org.wartremover.warts.While", "org.wartremover.warts.Var", "org.wartremover.warts.Throw"))
case class DPJobLive(client: JobControllerClient) extends DPJobApi[Task] {

  def trackJob(project: String, region: String, job: Job): Task[Unit] = ZIO.attempt {
    val jobId    = job.getReference.getJobId
    var continue = true
    var jobInfo  = client.getJob(project, region, jobId)
    var jobState = jobInfo.getStatus.getState.toString
    while (continue) {
      jobInfo = client.getJob(project, region, jobId)
      jobState = jobInfo.getStatus.getState.toString
      logger.info(s"Job $jobId Status $jobState")
      jobInfo.getStatus.getState.toString match {
        case "DONE" =>
          logger.info(s"Job $jobId completed successfully with state $jobState")
          continue = false
        case "CANCELLED" | "ERROR" =>
          val error = jobInfo.getStatus.getDetails
          logger.error(s"Job $jobId failed with error $error")
          throw new RuntimeException(s"Job failed with error $error")
        case _ =>
          TimeUnit.SECONDS.sleep(10)
      }
    }
  }

  def submitSparkJob(
      args: List[String],
      mainClass: String,
      libs: List[String],
      conf: Map[String, String],
      cluster: String,
      project: String,
      region: String
  ): Task[Job] = ZIO.attempt {
    logger.info(s"""Trying to submit spark job on Dataproc with Configurations:
                   |region => $region
                   |project => $project
                   |cluster => $cluster
                   |mainClass => $mainClass
                   |args => $args
                   |conf => $conf""".stripMargin)
    logger.info("libs")
    libs.foreach(logger.info)

    val jobPlacement = JobPlacement.newBuilder().setClusterName(cluster).build()
    val sparkJob = SparkJob
      .newBuilder()
      .addAllJarFileUris(libs.asJava)
      .putAllProperties(conf.asJava)
      .setMainClass(mainClass)
      .addAllArgs(args.asJava)
      .build()
    val job: Job = Job.newBuilder().setPlacement(jobPlacement).setSparkJob(sparkJob).build()
    client.submitJob(project, region, job)
  }

  def submitHiveJob(query: String, cluster: String, project: String, region: String): Task[Job] = ZIO.attempt {
    logger.info(s"""Trying to submit hive job on Dataproc with Configurations:
                   |region => $region
                   |project => $project
                   |cluster => $cluster
                   |query => $query""".stripMargin)
    val jobPlacement = JobPlacement.newBuilder().setClusterName(cluster).build()
    val queryList    = QueryList.newBuilder().addQueries(query)
    val hiveJob      = HiveJob.newBuilder().setQueryList(queryList).build()
    val job: Job     = Job.newBuilder().setPlacement(jobPlacement).setHiveJob(hiveJob).build()
    client.submitJob(project, region, job)
  }
}

object DPJobLive {
  def apply(endpoint: String): TaskLayer[DPJobEnv] =
    ZLayer.scoped(ZIO.fromAutoCloseable(DPJobClient(endpoint)).map(dp => DPJobLive(dp)))
}
