package restactor.impl.v2

import akka.actor.Actor.Receive
import akka.actor.PoisonPill
import play.api.Logging
import restactor.service.CallResult
import restactor.utils.{ActorState, RequestMessage}

class GiveUpState(val actor : ChildActor, err: Throwable) extends ActorState with StateHelper with Logging{

  override def onEnter(): Unit = {
    logger.info("become giveup state")
    self ! PoisonPill
  }

  override def receive: Receive = {
    case req: MessageCallArgs => {
      req.sendResponse(sender(), CallResult(500, "system error"))
    }
    case req: RequestMessage => {
      req.sendError(sender(), err)
    }
    case _ => {}
  }
}
