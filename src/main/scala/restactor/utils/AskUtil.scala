package restactor.utils

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef}
import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

trait ActorState {
  def onEnter() : Unit = {

  }

  def onExit() : Unit = {

  }

  def receive: Receive
}

abstract  class StateMachineActor extends  Actor {

  var currentState : ActorState = _
  def become( news:  => ActorState) : Unit = {
    if(currentState != null) {
      currentState.onExit()
    }
    val ns = news
    context.become(ns.receive)
    currentState = ns
    ns.onEnter()
  }
}

trait ResponseMesaageType[T] {
  def sendResponse( actorRef: ActorRef , response : T ) = {
    actorRef ! Success(response)
  }

  def sendResponse( actorRef: ActorRef , response : Throwable) = {
    actorRef ! Failure(response)
  }

  def sendResponse( actorRef: ActorRef , response : Try[T]) = {
    actorRef ! response
  }
}

object askUtil {
  import akka.pattern.ask

  implicit class AskUtil ( actorRef : ActorRef ) {
    def askWith[T](responseMessageType: ResponseMesaageType[T])(implicit timeout: Timeout, sender: ActorRef = Actor.noSender, classTag : ClassTag[T], executionContext: ExecutionContext) : Future[T] = {
      actorRef.ask( responseMessageType ).mapTo[Try[T]].flatMap {
        case Success(value) => Future.successful(value)
        case Failure(exception) => Future.failed(exception)
      }
    }
  }
}
