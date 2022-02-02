import zio.test._

object TestSuite extends DefaultRunnableSpec {
  override def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("Test Suite")(
      test("Test 1") {
        assertTrue(1 == 1)
      }
    ) @@ TestAspect.sequential
}
