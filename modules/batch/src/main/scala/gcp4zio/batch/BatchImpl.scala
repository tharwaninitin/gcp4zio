package gcp4zio.batch

import com.google.cloud.batch.v1.AllocationPolicy.{InstancePolicy, InstancePolicyOrTemplate, NetworkInterface, NetworkPolicy}
import com.google.cloud.batch.v1.LogsPolicy.Destination
import com.google.cloud.batch.v1.Runnable.Container
import com.google.cloud.batch.v1._
import com.google.protobuf.Duration
import zio.{Task, ZIO}
import scala.jdk.CollectionConverters._

case class BatchImpl(client: BatchServiceClient, project: String, region: String) extends Batch {
  override def createJob(
      name: String,
      image: String,
      commands: List[String],
      entrypoint: Option[String],
      serviceAccount: Option[String]
  ): Task[Job] =
    ZIO.attempt {
      // Define what will be done as part of the job.

      val container = entrypoint match {
        case Some(value) =>
          Container.newBuilder().setImageUri(image).addAllCommands(commands.asJava).setEntrypoint(value).build()
        case None => Container.newBuilder().setImageUri(image).addAllCommands(commands.asJava).build()
      }

      val runnable = Runnable.newBuilder().setContainer(container).build()

      // We can specify what resources are requested by each task.
      // In milliseconds per cpu-second. This means the task requires 2 whole CPUs.
      // In MiB.
      val computeResource = ComputeResource.newBuilder().setCpuMilli(2000).setMemoryMib(16).build()

      // Jobs can be divided into tasks. In this case, we have only one task.
      val task = TaskSpec
        .newBuilder()
        .addRunnables(runnable)
        .setComputeResource(computeResource)
        .setMaxRetryCount(0)
        .setMaxRunDuration(Duration.newBuilder().setSeconds(3600).build())
        .build()

      // Tasks are grouped inside a job using TaskGroups.
      // Currently, it's possible to have only one task group.
      val taskGroup = TaskGroup.newBuilder().setTaskCount(1).setTaskSpec(task).build()

      // Policies are used to define on what kind of virtual machines the tasks will run on.
      // In this case, we tell the system to use "e2-standard-4" machine type.
      // Read more about machine types here: https://cloud.google.com/compute/docs/machine-types
      val instancePolicy = InstancePolicy.newBuilder().setMachineType("e2-standard-4").build()

      val sa = serviceAccount.map(ServiceAccount.newBuilder().setEmail(_).build())

      val ni = NetworkInterface
        .newBuilder()
        .setNetwork("global/networks/default")
        .setSubnetwork("regions/us-central1/subnetworks/default")
        .setNoExternalIpAddress(true)
        .build()

      val np = NetworkPolicy.newBuilder().addNetworkInterfaces(ni)

      val allocationPolicy =
        sa match {
          case Some(value) =>
            AllocationPolicy
              .newBuilder()
              .addInstances(InstancePolicyOrTemplate.newBuilder().setPolicy(instancePolicy).build())
              .setServiceAccount(value)
              .setNetwork(np)
              .build()
          case None =>
            AllocationPolicy
              .newBuilder()
              .addInstances(InstancePolicyOrTemplate.newBuilder().setPolicy(instancePolicy).build())
              .setNetwork(np)
              .build()
        }

      val job = Job
        .newBuilder()
        .addTaskGroups(taskGroup)
        .setAllocationPolicy(allocationPolicy)
        .putLabels("env", "testing")
        .putLabels("type", "container")
        // We use Cloud Logging as it's an out of the box available option.
        .setLogsPolicy(LogsPolicy.newBuilder().setDestination(Destination.CLOUD_LOGGING).build())
        .build()

      val createJobRequest = CreateJobRequest
        .newBuilder()
        // The job's parent is the region in which the job will run.
        .setParent(String.format("projects/%s/locations/%s", project, region))
        .setJob(job)
        .setJobId(name)
        .build()

      logger.info(s"Creating batch job")

      val jobResult = client.createJobCallable().call(createJobRequest)

      logger.info(s"Successfully created the job ${jobResult.getName}")

      jobResult
    }

  override def deleteJob(name: String): Task[Unit] = {
    val jobName = s"projects/$project/locations/$region/jobs/$name"
    logger.info(s"Deleting batch job $jobName")
    ZIO.fromFutureJava(client.deleteJobAsync(jobName)) *> ZIO.logInfo(s"Deleted batch job: $jobName")
  }

  override def listJobs: Task[Iterable[Job]] = ZIO.attempt {
    val parent = s"projects/$project/locations/$region"
    client.listJobs(parent).iterateAll().asScala
  }
}
