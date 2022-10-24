package gcp4zio

import gcp4zio.pubsub.subscription.PSSubscription
import gcp4zio.pubsub.topic.PSTopic
import org.slf4j.{Logger, LoggerFactory}
import zio.Task

package object pubsub {
  lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  type PSTopicEnv = PSTopic[Task]
  type PSSubEnv   = PSSubscription[Task]
}
