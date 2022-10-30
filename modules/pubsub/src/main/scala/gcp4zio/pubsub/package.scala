package gcp4zio

import org.slf4j.{Logger, LoggerFactory}

package object pubsub {
  private[pubsub] lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)
}
