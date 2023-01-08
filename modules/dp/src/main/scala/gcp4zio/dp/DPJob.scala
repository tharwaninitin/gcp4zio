package gcp4zio
package dp

import com.google.cloud.dataproc.v1.Job
import zio.{RIO, Task, TaskLayer, ZIO, ZLayer}
import java.time.Duration

trait DPJob {

  /** Submits a Spark Job in Dataproc Cluster. (This API will not wait for Job Completion, to wait for Job completion use
    * [[trackJobProgress]])
    * @param args
    *   command line arguments which will be passed to spark application
    * @param mainClass
    *   Main class to run
    * @param libs
    *   List of jar required to run this Spark Application (including application jar)
    * @param conf
    *   Key value pair of the spark properties
    * @return
    */
  def submitSparkJob(args: List[String], mainClass: String, libs: List[String], conf: Map[String, String]): Task[Job]

  /** Submits a Hive Job in Dataproc Cluster. (This API will not wait for Job Completion, to wait for Job completion use
    * [[trackJobProgress]])
    * @param query
    *   Hive SQL query to run
    * @return
    */
  def submitHiveJob(query: String): Task[Job]

  /** This API will track job until Completion
    * @param job
    *   Dataproc Job which needs to be tracked
    * @param interval
    *   Specifies duration each status check repetition should be spaced from the last run
    * @return
    */
  def trackJobProgress(job: Job, interval: Duration): Task[Unit]
}

object DPJob {

  /** Submits a Spark Job in Dataproc Cluster. (This API will not wait for Job Completion, to wait for Job completion use
    * [[trackJobProgress]])
    * @param args
    *   command line arguments which will be passed to spark application
    * @param mainClass
    *   Main class to run
    * @param libs
    *   List of jar required to run this Spark Application (including application jar)
    * @param conf
    *   Key value pair of the spark properties
    * @return
    */
  def submitSparkJob(args: List[String], mainClass: String, libs: List[String], conf: Map[String, String]): RIO[DPJob, Job] =
    ZIO.environmentWithZIO(_.get.submitSparkJob(args, mainClass, libs, conf))

  /** Submits a Spark Job in Dataproc Cluster and waits till Job Completion
    * @param args
    *   command line arguments which will be passed to spark application
    * @param mainClass
    *   Main class to run
    * @param libs
    *   List of jar required to run this Spark Application (including application jar)
    * @param conf
    *   Key value pair of the spark properties
    * @param trackingInterval
    *   Specifies duration each status check repetition should be spaced from the last run
    * @return
    */
  def executeSparkJob(
      args: List[String],
      mainClass: String,
      libs: List[String],
      conf: Map[String, String],
      trackingInterval: Duration = Duration.ofSeconds(10)
  ): RIO[DPJob, Job] =
    for {
      job <- ZIO.environmentWithZIO[DPJob](_.get.submitSparkJob(args, mainClass, libs, conf))
      _   <- ZIO.environmentWithZIO[DPJob](_.get.trackJobProgress(job, trackingInterval))
    } yield job

  /** Submits a Hive Job in Dataproc Cluster. (This API will not wait for Job Completion, to wait for Job completion use
    * [[trackJobProgress]])
    * @param query
    *   Hive SQL query to run
    * @return
    */
  def submitHiveJob(query: String): RIO[DPJob, Job] = ZIO.environmentWithZIO(_.get.submitHiveJob(query))

  /** Submits a Hive Job in Dataproc Cluster. (This API will not wait for Job Completion, to wait for Job completion use
    * [[trackJobProgress]])
    * @param query
    *   Hive SQL query to run
    * @param trackingInterval
    *   Specifies duration each status check repetition should be spaced from the last run
    * @return
    */
  def executeHiveJob(query: String, trackingInterval: Duration = Duration.ofSeconds(10)): RIO[DPJob, Job] =
    for {
      job <- ZIO.environmentWithZIO[DPJob](_.get.submitHiveJob(query))
      _   <- ZIO.environmentWithZIO[DPJob](_.get.trackJobProgress(job, trackingInterval))
    } yield job

  /** This API will track job until Completion
    * @param job
    *   Dataproc Job which needs to be tracked
    * @param interval
    *   Specifies duration each status check repetition should be spaced from the last run
    * @return
    */
  def trackJobProgress(job: Job, interval: Duration = Duration.ofSeconds(10)): RIO[DPJob, Unit] =
    ZIO.environmentWithZIO(_.get.trackJobProgress(job, interval))

  /** Creates live layer required for all [[DPJob]] API's
    * @param cluster
    *   Dataproc cluster name
    * @param project
    *   GCP projectID
    * @param region
    *   GCP Region name
    * @param endpoint
    *   GCP dataproc API
    * @return
    */
  def live(cluster: String, project: String, region: String, endpoint: String): TaskLayer[DPJob] =
    ZLayer.scoped(DPJobClient(endpoint).map(dp => DPJobImpl(dp, cluster, project, region)))
}
