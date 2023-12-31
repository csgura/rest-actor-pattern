package restactor.utils

import akka.actor.Actor
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import play.api.Logging

trait ActorState {
  def onEnter(): Unit = {}

  def onExit(): Unit = {}

  def postStop() : Unit = {}

  def receive: Actor.Receive
}

abstract class StateMachineActor extends Actor with Logging {

  implicit val dispatcher: ExecutionContext = context.dispatcher

  var currentState: ActorState = _
  def become(news: => ActorState): Unit = {
    if (currentState != null) {
      currentState.onExit()
    }
    val ns = news
    context.become(ns.receive.orElse(statelessReceive))
    currentState = ns
    ns.onEnter()
  }

  def pipeMe[T](f: Future[T]): Future[T] = {
    f.foreach(v => self ! v)
    f.failed.foreach(err => self ! err)
    f
  }

  def pipeMe[T, E](f: Future[T], maperror: Throwable => E): Future[T] = {
    f.foreach(v => self ! v)
    f.failed.foreach(err => self ! maperror(err))
    f
  }

  override def postStop(): Unit = {
    if (currentState != null) {
      currentState.onExit()
      currentState.postStop()
      currentState = null
    }
    logger.info(s"actor ${self} stopped")
  }

  def statelessReceive : Receive = {
    case any => {
      logger.info(s"actor $self receives unknown message $any")
    }
  }

  override def receive: Receive = statelessReceive
}
