package gcp4zio.pubsub.topic

import com.google.iam.v1.Policy
import com.google.pubsub.v1.Topic
import zio._

trait PSTopic {

  /** @param project
    *   GCP Project ID
    * @param topic
    *   The name of the topic to be created
    * @return
    */
  def createTopic(project: String, topic: String): Task[Topic]

  /** @param project
    *   GCP Project ID
    * @param topic
    *   The name of the topic to be deleted
    * @return
    */
  def deleteTopic(project: String, topic: String): Task[Unit]

  /** @param project
    *   GCP Project ID
    * @param topic
    *   The name of topic for policy update
    * @param member
    *   serviceAccount or userAccount name
    * @param role
    *   role which needs to be assigned
    * @return
    *   Updated IAM policy
    */
  def addIAMPolicyBindingToTopic(project: String, topic: String, member: String, role: String): Task[Policy]
}

object PSTopic {

  /** @param project
    *   GCP Project ID
    * @param topic
    *   The name of the topic to be created
    * @return
    */
  def createTopic(project: String, topic: String): ZIO[PSTopic, Throwable, Topic] =
    ZIO.environmentWithZIO(_.get.createTopic(project, topic))

  /** @param project
    *   GCP Project ID
    * @param topic
    *   The name of the topic to be deleted
    * @return
    */
  def deleteTopic(project: String, topic: String): ZIO[PSTopic, Throwable, Unit] =
    ZIO.environmentWithZIO(_.get.deleteTopic(project, topic))

  /** @param project
    *   GCP Project ID
    * @param topic
    *   The name of topic for policy update
    * @param member
    *   serviceAccount or userAccount name
    * @param role
    *   role which needs to be assigned
    * @return
    *   Updated IAM policy
    */
  def addIAMPolicyBindingToTopic(project: String, topic: String, member: String, role: String): ZIO[PSTopic, Throwable, Policy] =
    ZIO.environmentWithZIO(_.get.addIAMPolicyBindingToTopic(project, topic, member, role))

  /** @param path
    *   Optional path to google service account credential file
    * @return
    *   PSTopic
    */
  def live(path: Option[String] = None): TaskLayer[PSTopic] =
    ZLayer.scoped(PSTopicClient(path).map(client => PSTopicImpl(client)))

  /** Test layer
    *
    * @return
    *   PSTopic
    */
  val test: TaskLayer[PSTopic] = ZLayer.scoped(PSTopicClient.testClient.map(client => PSTopicImpl(client)))
}
