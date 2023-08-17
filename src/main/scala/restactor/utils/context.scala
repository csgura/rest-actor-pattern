package restactor.utils


import java.util.concurrent.TimeoutException
import scala.concurrent.{ExecutionContext, Promise}
import scala.util.{Failure, Success, Try}

object context {

  trait ContextKey[T]

  trait Context {
    def Deadline(): Option[Long]
    def Done( cb : () => Unit)(implicit ec : ExecutionContext) : Unit
    def Err(): Try[Unit]
    def Value[T](key: ContextKey[T]): Option[T]
  }

  def Background() : Context = {
    empty
  }

  def WithValue[A](parent : Context, key : ContextKey[A], value : A) : Context = {
    withValue(parent,key,value)
  }

  type CancelFunc = () => Unit

  def WithDeadline(parent: Context, deadline : Long ) : (Context, CancelFunc) = {
    val cancel = Promise[Unit]()
    (withDeadline(parent,deadline, cancel) , () => { cancel.success(()) })
  }

  private case class withValue[A](parent: Context, key: ContextKey[A], value: A) extends Context {
    override def Deadline(): Option[Long] = parent.Deadline()

    override def Done(cb: () => Unit)(implicit ec: ExecutionContext): Unit = parent.Done(cb)

    override def Err(): Try[Unit] = parent.Err()

    override def Value[T](key: ContextKey[T]): Option[T] = {
      if (this.key == key) {
        Some(value.asInstanceOf[T])
      } else {
        parent.Value(key)
      }
    }
  }

  private case class withDeadline(parent: Context, deadline : Long, cancel : Promise[Unit]) extends Context {
    override def Deadline(): Option[Long] = Some(deadline)

    override def Done(cb: () => Unit)(implicit ec : ExecutionContext): Unit = cancel.future.foreach(_ => cb())

    override def Err(): Try[Unit] = {
      if ( System.currentTimeMillis() < deadline && !cancel.isCompleted) {
        Success(())
      } else {
        if(!cancel.isCompleted) {
          cancel.success(())
        }
        Failure(new TimeoutException("context.deadline"))
      }
    }
    override def Value[T](key: ContextKey[T]): Option[T] = parent.Value(key)
  }

  private case object empty extends Context {
    override def Deadline(): Option[Long] = None

    override def Done(cb: () => Unit)(implicit ec : ExecutionContext): Unit = {
    }

    override def Err(): Try[Unit] = {
      Success(())
    }

    override def Value[T](key: ContextKey[T]): Option[T] = None
  }

}
