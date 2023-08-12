package restactor.utils

import akka.actor.Actor
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

trait ActorState {
  def onEnter(): Unit = {}

  def onExit(): Unit = {}

  def receive: Actor.Receive
}

abstract class StateMachineActor extends Actor {

  implicit val dispatcher: ExecutionContext = context.dispatcher

  var currentState: ActorState = _
  def become(news: => ActorState): Unit = {
    if (currentState != null) {
      currentState.onExit()
    }
    val ns = news
    context.become(ns.receive)
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
}
