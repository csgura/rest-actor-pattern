package restactor.utils

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef}
import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}
import views.html.defaultpages.error

trait RequestMessage {
  def sendError(actorRef: ActorRef, err: Throwable) = {
    actorRef ! Failure(err)
  }
}

trait ResponseMesaageType[T] extends RequestMessage {
  def sendResponse(actorRef: ActorRef, response: T) = {
    actorRef ! Success(response)
  }

  def sendResponse(actorRef: ActorRef, response: Try[T]) = {
    actorRef ! response
  }

  def sendFuture(actorRef: ActorRef, f: Future[T])(implicit
      ec: ExecutionContext
  ) = {
    f.onComplete(sendResponse(actorRef, _))
  }
}

object askUtil {
  import akka.pattern.ask

  implicit class AskUtil(actorRef: ActorRef) {
    def askWith[T](responseMessageType: ResponseMesaageType[T])(implicit
        timeout: Timeout,
        sender: ActorRef = Actor.noSender,
        classTag: ClassTag[T],
        executionContext: ExecutionContext
    ): Future[T] = {
      actorRef.ask(responseMessageType).mapTo[Try[T]].flatMap {
        case Success(value)     => Future.successful(value)
        case Failure(exception) => Future.failed(exception)
      }
    }
  }
}
