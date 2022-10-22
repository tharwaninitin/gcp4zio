package gcp4zio

import org.slf4j.{Logger, LoggerFactory}
import zio.Task

package object pubsub {
  lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  type PSTopicEnv = PSTopicApi[Task]
  type PSSubEnv   = PSSubApi[Task]
}
