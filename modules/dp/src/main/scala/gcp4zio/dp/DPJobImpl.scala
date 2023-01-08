package gcp4zio
package dp

import com.google.cloud.dataproc.v1._
import zio.Schedule.Decision
import zio.{Duration, Schedule, Task, ZIO}
import scala.jdk.CollectionConverters._

@SuppressWarnings(Array("org.wartremover.warts.ToString"))
case class DPJobImpl(client: JobControllerClient, cluster: String, project: String, region: String) extends DPJob {

  private val recurWhile: Schedule[Any, Job, Job] = Schedule.recurWhile[Job] {
    case job if job.getStatus.getState.name() == "CANCELLED" || job.getStatus.getState.name() == "ERROR" =>
      false // Stop this schedule in case of job failure or cancellation
    case _ => true
  }

  private def finalSchedule(interval: Duration): Schedule.WithState[(recurWhile.State, Long), Any, Job, (Job, Long)] =
    (recurWhile && Schedule.spaced(interval)).onDecision {
      case (_, out, Decision.Done) => ZIO.succeed(logger.error(s"Job failed, JOB_STATE => ${out._1.getStatus.getState.name()}"))
      case (_, out, Decision.Continue(_)) =>
        ZIO.succeed(logger.info(s"Progress Check #${out._2 + 1}, JOB_STATE => ${out._1.getStatus.getState.name()} "))
    }

  def trackJobProgress(job: Job, interval: Duration): Task[Unit] = {
    val jobId = job.getJobUuid
    logger.info(s"Started tracking progress for job $jobId")
    ZIO
      .succeed(client.getJob(project, region, jobId))
      .flatMap { jobResponse =>
        if (jobResponse.getStatus.getState.name() == "DONE") ZIO.succeed(jobResponse)
        else ZIO.fail(jobResponse)
      }
      .retry(finalSchedule(interval))
      .mapBoth(
        errorJob => {
          logger.error(errorJob.toString)
          new RuntimeException(s"Job failed, ${errorJob.getStatus.getDetails}")
        },
        successJob => {
          logger.info(successJob.toString)
          logger.info(s"Job completed successfully")
        }
      )
  }

  def submitSparkJob(args: List[String], mainClass: String, libs: List[String], conf: Map[String, String]): Task[Job] =
    ZIO.attempt {
      logger.info(s"""Submitting spark job on dataproc with below configurations:
                     |region => $region
                     |project => $project
                     |cluster => $cluster
                     |mainClass => $mainClass
                     |args => $args
                     |conf => $conf
                     |libs => ${libs.mkString(",")}""".stripMargin)

      val jobPlacement = JobPlacement.newBuilder().setClusterName(cluster).build()
      val sparkJob = SparkJob
        .newBuilder()
        .addAllJarFileUris(libs.asJava)
        .putAllProperties(conf.asJava)
        .setMainClass(mainClass)
        .addAllArgs(args.asJava)
        .build()
      val jobRequest: Job = Job.newBuilder().setPlacement(jobPlacement).setSparkJob(sparkJob).build()
      val jobResponse     = client.submitJob(project, region, jobRequest)
      logger.info(s"Spark job submitted successfully with JobId ${jobResponse.getJobUuid}")
      jobResponse
    }

  def submitHiveJob(query: String): Task[Job] = ZIO.attempt {
    logger.info(s"""Submitting hive job on dataproc with below configurations:
                   |region => $region
                   |project => $project
                   |cluster => $cluster
                   |query => $query""".stripMargin)
    val jobPlacement    = JobPlacement.newBuilder().setClusterName(cluster).build()
    val queryList       = QueryList.newBuilder().addQueries(query)
    val hiveJob         = HiveJob.newBuilder().setQueryList(queryList).build()
    val jobRequest: Job = Job.newBuilder().setPlacement(jobPlacement).setHiveJob(hiveJob).build()
    val jobResponse     = client.submitJob(project, region, jobRequest)
    logger.info(s"Hive job submitted successfully with JobId ${jobResponse.getJobUuid}")
    jobResponse
  }
}
