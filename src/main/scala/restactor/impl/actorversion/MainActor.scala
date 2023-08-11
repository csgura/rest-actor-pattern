package restactor.impl.actorversion

import akka.actor.{Actor, Props}
import restactor.service.{CallArgs, CallResult}
import restactor.utils.ResponseMesaageType

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
case class MessageCallArgs( requestTime : Long, callArgs : CallArgs ) extends HasTarget with ResponseMesaageType[CallResult] {
  override def targetId: String = callArgs.targetId
}
