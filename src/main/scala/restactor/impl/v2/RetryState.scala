package restactor.impl.v2

import akka.actor.Actor.Receive
import play.api.Logging
import restactor.service.DeviceInfo
import restactor.utils.{ActorState, RequestMessage}

import scala.concurrent.ExecutionContext

case class DeviceLoadErr(throwable: Throwable)

class RetryState(val actor : ChildActor, retryCount : Int) extends ActorState with StateHelper with Logging {

  implicit val dispatcher : ExecutionContext = actor.dispatcher

  def loadDeviceInfo(): Unit = {
    logger.info("load device info")
    val f = actor.requires.deviceDao.loadInfo(actor.targetId);
    actor.pipeMe(f, DeviceLoadErr)

  }


  override def onEnter(): Unit = {
    logger.info(s"become retry state, retryCount = ${retryCount}")

    loadDeviceInfo()
  }

  override def receive: Receive = {
    case di: DeviceInfo => {
      actor.become(new OkState(actor, di))
      actor.unstashAll()
    }
    case err: DeviceLoadErr => {
      actor.unstashAll()
      if (retryCount >= 2) {
        actor.become(new GiveUpState(actor, err.throwable))
      } else {
        actor.become(new FailedState(actor, retryCount, err.throwable))
      }
    }
    case _: RequestMessage => {
      actor.stash()
    }
  }
}
