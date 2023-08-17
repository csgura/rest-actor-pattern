package restactor.impl.v2

import akka.actor.Actor.Receive
import akka.actor.{ActorContext, ActorRef}
import play.api.Logging
import restactor.service.CallResult
import restactor.utils.{ActorState, StateMachineActor}

import scala.concurrent.duration.DurationInt

trait StateHelper {
  def actor : ChildActor
  def context: ActorContext = actor.context
  def self : ActorRef = actor.self
  def sender() : ActorRef = context.sender()
}

class FailedState(val actor : ChildActor,retryCount: Int, err: Throwable ) extends ActorState with StateHelper with Logging {

  override def onEnter(): Unit = {
    logger.info("become failed state")
    actor.timers.startSingleTimer("reconnect", "reconnect", 2.second)
  }

  override def onExit(): Unit = actor.timers.cancel("reconnect")

  override def receive: Receive = {
    case "reconnect" => {
      // timer 이벤트 아닌 다른 이벤틀 받고 , become 하는 경우는 cancel timer 후에 become 해야 함
      // timers.cancel("reconnect")

      // 여기는 timer event 받고 이동하는 거라 cancel 할 필요가 없음.
      actor.become(new RetryState(actor, retryCount + 1))
    }
    case req: MessageCallArgs => {
      if (req.ctx.Err().isFailure) {
        req.sendResponse(
          sender(),
          CallResult(500, s"err = ${err.getMessage}")
        )
      } else {
        actor.stash()
      }
    }
  }
}
