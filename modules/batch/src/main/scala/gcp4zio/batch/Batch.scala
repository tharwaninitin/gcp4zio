package gcp4zio.batch

import com.google.cloud.batch.v1.Job
import zio.{RIO, Task, TaskLayer, ZIO, ZLayer}

trait Batch {
  def createJob(
      name: String,
      image: String,
      commands: List[String],
      entrypoint: Option[String],
      serviceAccount: Option[String]
  ): Task[Job]

  def deleteJob(name: String): Task[Unit]

  def listJobs: Task[Iterable[Job]]
}

object Batch {
  def createJob(
      name: String,
      image: String,
      commands: List[String],
      entrypoint: Option[String],
      serviceAccount: Option[String]
  ): RIO[Batch, Job] =
    ZIO.serviceWithZIO(_.createJob(name, image, commands, entrypoint, serviceAccount))

  def deleteJob(name: String): RIO[Batch, Unit] = ZIO.serviceWithZIO(_.deleteJob(name))

  def listJobs: RIO[Batch, Iterable[Job]] = ZIO.serviceWithZIO(_.listJobs)

  def live(project: String, region: String): TaskLayer[Batch] =
    ZLayer.scoped(BatchClient().map(batch => BatchImpl(batch, project, region)))
}
