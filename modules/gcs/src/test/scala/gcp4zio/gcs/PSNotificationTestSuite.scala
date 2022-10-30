package gcp4zio.gcs

import com.google.cloud.storage.NotificationInfo
import gcp4zio.Global._
import zio.ZIO
import zio.test.Assertion.{containsString, equalTo, isNull}
import zio.test.{assertZIO, suite, test, Spec, TestAspect}

@SuppressWarnings(Array("org.wartremover.warts.AutoUnboxing"))
object PSNotificationTestSuite {
  val spec: Spec[GCS, Any] =
    suite("GCSNotificationConfiguration Apis")(
      test("Execute createNotificationConfiguration with existing topic") {
        val notification = GCS.createPSNotification(gcsBucket, validTopic)
        assertZIO(notification.foldZIO(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op.getTopic)))(containsString(validTopic))
      },
      test("Execute createNotificationConfiguration with existing topic for Create Object Event") {
        val notification = GCS
          .createPSNotification(gcsBucket, validTopic, eventType = Some(NotificationInfo.EventType.OBJECT_FINALIZE))
        assertZIO(notification.foldZIO(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op.getTopic)))(containsString(validTopic))
      },
      test("Execute createNotificationConfiguration with not existing topic") {
        val notification = GCS.createPSNotification(gcsBucket, notValidTopic)
        val error        = "not found"
        assertZIO(notification.foldZIO(ex => ZIO.succeed(ex.getMessage), _ => ZIO.fail("ok")))(containsString(error))
      },
      test("Execute getNotificationConfiguration with existing notification") {
        val notification = GCS.getPSNotification(gcsBucket, validNotificationId)
        assertZIO(notification.foldZIO(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op.getTopic)))(containsString("topics"))
      },
      test("Execute getNotificationConfiguration with not existing notification") {
        val notification = GCS.getPSNotification(gcsBucket, notValidNotificationId)
        assertZIO(notification.foldZIO(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op)))(isNull)
      },
      test("Execute listNotificationConfiguration") {
        val notificationInfoList = GCS.listPSNotification(gcsBucket)
        assertZIO(notificationInfoList.foldZIO(ex => ZIO.fail(ex.toString), _ => ZIO.succeed("ok")))(equalTo("ok"))
      },
      test("Execute deleteNotificationConfiguration with existing notification") {
        val notification = GCS.deletePSNotification(gcsBucket, validNotificationId)
        assertZIO(notification.foldZIO(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op)))(equalTo(true))
      },
      test("Execute deleteNotificationConfiguration with not existing notification") {
        val notification = GCS.deletePSNotification(gcsBucket, notValidNotificationId)
        assertZIO(notification.foldZIO(ex => ZIO.fail(ex.toString), op => ZIO.succeed(op)))(equalTo(false))
      }
    ) @@ TestAspect.sequential
}
