import gcp4zio.{GCSEnv, TestHelper}
import utils.ApplicationLogger
import zio._
import zio.stream._
import zio.test._

object ProcessStreamsTestSuite extends TestHelper with ApplicationLogger {
  case class Parsed(value: String) extends AnyVal

  val spec: ZSpec[environment.TestEnvironment with GCSEnv, Any] =
    testM("Execute process streams") {
      val transducer: Transducer[Throwable, Byte, Either[Throwable, Parsed]] =
        ZTransducer.utf8Decode >>> ZTransducer.splitLines.map(line => Right(Parsed(line)))

      val step = ProcessStreams[Any, Parsed](
        gcs_bucket,
        "temp/test/",
        transducer,
        p => UIO(logger.info(p.toString)),
        ex => UIO(logger.info(ex.getMessage))
      )

      step.start.foldM(ex => ZIO.fail(ex.getMessage), _ => ZIO.succeed("ok")).as(assertCompletes)
    }
}
