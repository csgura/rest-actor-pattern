package test.context

import org.scalatest.funsuite.AnyFunSuite
import restactor.utils.context


class TestContext extends AnyFunSuite {
  case object TraceKey extends context.ContextKey[String]

  test("test context") {
    val ctx = context.WithValue(context.Background(),TraceKey, "hello")

    assert(  ctx.Value(TraceKey).contains("hello") )

    val (ctx2, cf) = context.WithDeadline(ctx, System.currentTimeMillis() + 1000)

    assert(ctx2.Deadline().isDefined)
    assert(ctx2.Err().isSuccess)

    cf()
    assert(ctx2.Err().isFailure)


  }

}
