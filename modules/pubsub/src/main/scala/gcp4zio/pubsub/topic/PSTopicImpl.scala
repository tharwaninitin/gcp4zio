package gcp4zio.pubsub.topic

import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.iam.v1.{Binding, GetIamPolicyRequest, Policy, SetIamPolicyRequest}
import com.google.pubsub.v1.{Topic, TopicName}
import zio._

@SuppressWarnings(Array("org.wartremover.warts.ToString"))
case class PSTopicImpl(client: TopicAdminClient) extends PSTopic {

  override def createTopic(projectId: String, topicId: String): Task[Topic] = ZIO.attempt {
    val topicName = TopicName.of(projectId, topicId)
    client.createTopic(topicName)
  }

  override def deleteTopic(project: String, topic: String): Task[Unit] = ZIO.attempt {
    val topicName = TopicName.of(project, topic)
    client.deleteTopic(topicName)
  }

  override def addIAMPolicyBindingToTopic(project: String, topic: String, member: String, role: String): Task[Policy] =
    ZIO.attempt {
      val topicName           = TopicName.of(project, topic)
      val getIamPolicyRequest = GetIamPolicyRequest.newBuilder().setResource(topicName.toString).build()
      val policy              = client.getIamPolicy(getIamPolicyRequest)
      val newBinding = Binding
        .newBuilder()
        .setRole(role)
        .addMembers(member)
        .build()
      val updatedPolicy = policy.toBuilder
        .addBindings(newBinding)
        .build()
      val setIamPolicyRequest = SetIamPolicyRequest.newBuilder().setPolicy(updatedPolicy).setResource(topicName.toString).build()
      client.setIamPolicy(setIamPolicyRequest)
    }
}
