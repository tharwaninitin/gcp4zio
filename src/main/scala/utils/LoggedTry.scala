package utils

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

object LoggedTry extends ApplicationLogger {
  def apply[A](
      computation: => A,
      failure: Throwable => Unit = e => logger.error(s"Failure: $e"),
      defect: Throwable => Unit = e => logger.error(s"Defect: $e")
  ): Try[A] =
    try Success(computation)
    catch {
      case NonFatal(e) =>
        failure(e)
        Failure(e)
      case e: Throwable =>
        defect(e)
        throw e
    }
}
