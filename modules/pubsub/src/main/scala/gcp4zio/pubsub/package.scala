package gcp4zio

import gcp4zio.utils.ApplicationLogger
import zio.Task

package object pubsub extends ApplicationLogger {
  type PubSubTopicEnv        = PubSubTopicApi[Task]
  type PubSubSubscriptionEnv = PubSubSubscriptionApi[Task]
}
