import org.slf4j.{Logger, LoggerFactory}
import zio.logging.backend.SLF4J
import zio.{Runtime, ULayer}

trait ApplicationLogger {
  protected val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  protected val zioSlf4jLogger: ULayer[Unit] = Runtime.removeDefaultLoggers >>> SLF4J.slf4j
}
