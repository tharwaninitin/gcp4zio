package gcp4zio

import org.slf4j.{Logger, LoggerFactory}

package object monitoring {
  private[monitoring] lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)
}
