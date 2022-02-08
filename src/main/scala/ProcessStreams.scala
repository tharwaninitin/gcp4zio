import gcp4zio._
import zio._
import zio.blocking.Blocking
import zio.stream._

case class ProcessStreams[R, T](
    input_bucket: String,
    input_path: String,
    transformation: ZTransducer[R, Throwable, Byte, Either[Throwable, T]],
    success_handler: T => UIO[Unit],
    error_handler: Throwable => UIO[Unit],
    parallelism: Int = 1,
    chunk_size: Int = 32 * 1024
) {
  final def start: RIO[R with GCSEnv with Blocking, Unit] = {
    val stream = Stream.fromEffect(UIO(logger.info("#" * 50))) ++
      ZStream
        .fromIteratorEffect(
          GCSApi
            .listObjects(input_bucket, input_path)
            .map(_.filter(f => !f.getName.endsWith("/") && f.getName.endsWith("csv")).map(_.getName).iterator)
        )
        .flatMapPar(parallelism) { path =>
          logger.info(path)
          GCSApi.getObject(input_bucket, path).transduce(transformation).drop(1)
        }
        .flatMap {
          case Left(ex)     => Stream.fromEffect(error_handler(ex))
          case Right(value) => Stream.fromEffect(success_handler(value))
        }

    stream.runDrain
  }
}
