package restactor.impl.v2

import akka.actor.{Actor, Props}
import restactor.service.{CallArgs, CallResult}
import restactor.utils.{ResponseMesaageType, context}


object MainActor {
  def props(requires: Requires): Props = {
    Props(new MainActor(requires))
  }
}

class MainActor(requires: Requires) extends Actor {
  override def receive: Receive = {
    case req : HasTarget => {
      val actorName = String.format("%s-%s", "rpc" , req.targetId)
      val childRef = context.child(actorName).getOrElse(context.actorOf(ChildActor.props(requires, req.targetId), actorName))

      childRef.forward(req)
    }
  }
}

trait HasTarget {
  def targetId : String
}

case class MessageCallArgs(ctx: context.Context, callArgs: CallArgs) extends ResponseMesaageType[CallResult] with HasTarget {
  override def targetId: String = callArgs.targetId
}
