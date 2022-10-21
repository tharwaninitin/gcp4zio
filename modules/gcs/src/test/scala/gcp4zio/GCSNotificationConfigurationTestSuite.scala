package gcp4zio

import com.google.cloud.storage.NotificationInfo
import gcp4zio.Global._
import gcp4zio.gcs.{GCSApi, GCSEnv}
import gcp4zio.utils.ApplicationLogger
import zio.ZIO
import zio.test.Assertion.{containsString, equalTo, isNull}
import zio.test._


@SuppressWarnings(Array("org.wartremover.warts.AutoUnboxing"))
object GCSNotificationConfigurationTestSuite extends ApplicationLogger {
  val validTopic = "projects/marksmng/topics/order_topic"
  val notValidTopic = "projects/marksmng/topics/notExistingTopic"
  val validNotificationId = "99"
  val notValidNotificationId = "0"
  val spec: Spec[GCSEnv, Any] =
    suite("GCSNotificationConfiguration Apis")(
      test("Execute createNotificationConfiguration with existing topic") {
        val notification = GCSApi.createNotificationConfiguration(gcsBucket, validTopic)
        assertZIO(notification.foldZIO(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op.getTopic)))(containsString(validTopic))
      },
      test("Execute createNotificationConfiguration with existing topic for Create Object Event") {
        val notification = GCSApi.createNotificationConfiguration(gcsBucket, validTopic, eventType = Some(NotificationInfo.EventType.OBJECT_FINALIZE))
        assertZIO(notification.foldZIO(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op.getTopic)))(containsString(validTopic))
      },
      test("Execute createNotificationConfiguration with not existing topic") {
        val notification = GCSApi.createNotificationConfiguration(gcsBucket, notValidTopic)
        val error = "not found"
        assertZIO(notification.foldZIO(ex => ZIO.succeed(ex.getMessage), _ => ZIO.fail("ok")))(containsString(error))
      },
      test("Execute getNotificationConfiguration with existing notification") {
        val notification = GCSApi.getNotificationConfiguration(gcsBucket, validNotificationId)
        assertZIO(notification.foldZIO(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op.getTopic)))(containsString(validTopic))
      },
      test("Execute getNotificationConfiguration with not existing notification") {
        val notification = GCSApi.getNotificationConfiguration(gcsBucket, notValidNotificationId)
        assertZIO(notification.foldZIO(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op)))(isNull)
      },
      test("Execute listNotificationConfiguration") {
        val notificationInfoList = GCSApi.listNotificationConfiguration(gcsBucket)
        assertZIO(notificationInfoList.foldZIO(ex => ZIO.fail(ex.toString), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Execute deleteNotificationConfiguration with existing notification") {
        val notification = GCSApi.deleteNotificationConfiguration(gcsBucket, validNotificationId)
        assertZIO(notification.foldZIO(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op)))(equalTo(true))
      },
      test("Execute deleteNotificationConfiguration with not existing notification") {
        val notification = GCSApi.deleteNotificationConfiguration(gcsBucket, notValidNotificationId)
        assertZIO(notification.foldZIO(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op)))(equalTo(false))
      }
    ) @@ TestAspect.sequential
}
