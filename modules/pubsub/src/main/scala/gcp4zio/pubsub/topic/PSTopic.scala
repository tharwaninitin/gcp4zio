package gcp4zio.pubsub.topic

import com.google.pubsub.v1.Topic
import gcp4zio.pubsub.PSTopicEnv
import zio._

trait PSTopic[F[_]] {

  /** @param project
    *   GCP Project ID
    * @param topic
    *   The name of the topic to be created
    * @return
    */
  def createTopic(project: String, topic: String): F[Topic]

  /** @param project
    *   GCP Project ID
    * @param topic
    *   The name of the topic to be created
    * @return
    */
  def deleteTopic(project: String, topic: String): F[Unit]
}

object PSTopic {

  /** @param project
    *   GCP Project ID
    * @param topic
    *   The name of the topic to be created
    * @return
    */
  def createTopic(project: String, topic: String): ZIO[PSTopicEnv, Throwable, Topic] =
    ZIO.environmentWithZIO(_.get.createTopic(project, topic))

  /** @param project
    *   GCP Project ID
    * @param topic
    *   The name of the topic to be created
    * @return
    */
  def deleteTopic(project: String, topic: String): ZIO[PSTopicEnv, Throwable, Unit] =
    ZIO.environmentWithZIO(_.get.deleteTopic(project, topic))

  /** @param path
    *   Optional path to google service account credential file
    * @return
    *   PSTopicEnv
    */
  def live(path: Option[String] = None): TaskLayer[PSTopicEnv] =
    ZLayer.scoped(PSTopicClient(path).map(client => PSTopicImpl(client)))

  /** Test layer
    *
    * @return
    *   PSTopicEnv
    */
  val test: TaskLayer[PSTopicEnv] =
    ZLayer.scoped(PSTopicClient.testClient.map(client => PSTopicImpl(client)))
}
