import zio._

object Main extends App with ApplicationLogger {

  val program = ZIO.succeed(logger.info("Hello World"))

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = program.exitCode
}
