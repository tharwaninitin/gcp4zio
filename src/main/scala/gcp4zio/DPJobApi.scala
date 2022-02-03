package gcp4zio

import zio.ZIO

object DPJobApi {
  trait Service[F[_]] {
    def executeSparkJob(
        args: List[String],
        main_class: String,
        libs: List[String],
        conf: Map[String, String],
        clusterName: String,
        project: String,
        region: String
    ): F[Unit]
    def executeHiveJob(query: String, clusterName: String, project: String, region: String): F[Unit]
  }

  def executeSparkJob(
      args: List[String],
      main_class: String,
      libs: List[String],
      conf: Map[String, String],
      clusterName: String,
      project: String,
      region: String
  ): ZIO[DPJobEnv, Throwable, Unit] =
    ZIO.accessM(_.get.executeSparkJob(args, main_class, libs, conf, clusterName, project, region))
  def executeHiveJob(query: String, clusterName: String, project: String, region: String): ZIO[DPJobEnv, Throwable, Unit] =
    ZIO.accessM(_.get.executeHiveJob(query, clusterName, project, region))
}
