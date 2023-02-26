package gcp4zio

import org.slf4j.{Logger, LoggerFactory}

package object batch {
  private[batch] lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)
}
