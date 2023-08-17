package restactor.impl.v2

import akka.actor.{Props, Stash, Timers}
import play.api.Logging
import restactor.service.{CallResult, DeviceInfo}
import restactor.utils.StateMachineActor

import scala.concurrent.Future

object ChildActor {
  def props(requires: Requires, targetId: String): Props = {
    Props(new ChildActor(requires, targetId))
  }

}



class ChildActor(val requires: Requires, val targetId: String)
  extends StateMachineActor
    with Stash
    with Timers
    with Logging {

  override def preStart(): Unit = become(new RetryState(this, 0 ))
}
