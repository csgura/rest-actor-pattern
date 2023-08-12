package restactor.impl.actorversion

import akka.actor.{Actor, Props, Stash, Timers}
import play.api.Logging
import restactor.service.{CallResult, DeviceInfo}
import restactor.utils.{ActorState, StateMachineActor}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import restactor.utils.RequestMessage

object ChildActor {
  def props(requires: Requires, targetId: String): Props = {
    Props(new ChildActor(requires, targetId))
  }

}

class ChildActor(requires: Requires, targetId: String)
    extends StateMachineActor
    with Stash
    with Timers
    with Logging {

  def loadDeviceInfo(): Unit = {
    logger.info("load device info")
    val f = requires.deviceDao.loadInfo(targetId);
    f.foreach(di => {
      self ! di
    })
    f.failed.foreach(err => {
      self ! err
    })
  }

  def callRpc(di: DeviceInfo, callArgs: MessageCallArgs): Future[CallResult] = {
    Future.successful(CallResult(di.num, "ok"))
  }
  def okState(di: DeviceInfo): ActorState = {
    return new ActorState {

      override def onEnter(): Unit = {
        context.setReceiveTimeout(2.second)
        requires.deviceDao.storeInfo(di)
      }

      override def onExit(): Unit = {
        requires.deviceDao.storeInfo(di)
      }

      override def receive: Receive = {
        case rpcCall: MessageCallArgs => {

          val returnPath = sender()
          val rfuture = callRpc(di, rpcCall)
          rfuture.foreach(res => rpcCall.sendResponse(returnPath, res))
          rfuture.failed.foreach(err => {
            rpcCall.sendError(returnPath, err)

            if (err.isInstanceOf[RuntimeException]) {
              self ! err
            }
          })

          logger.info(
            s"call incr num : ${di.num},  rpc : args : ${rpcCall.callArgs}"
          )

          rpcCall.sendResponse(sender(), CallResult(di.num, "ok"))
          become(okState(di.incrNum()))
        }
        case err: RuntimeException => {
          context.become(failedState(0, err))
        }
        case akka.actor.ReceiveTimeout => {
          requires.deviceDao.storeInfo(di)
          context.stop(self)
        }
      }
    }
  }

  def retryState(retryCount: Int): Receive = {
    logger.info("become retry state")

    loadDeviceInfo()
    return {
      case di: DeviceInfo => {
        become(okState(di))
        unstashAll()
      }
      case err: Throwable => {
        unstashAll()
        context.become(failedState(retryCount, err))
      }
      case _: RequestMessage => {
        stash()
      }
    }
  }

  def failedState(retryCount: Int, err: Throwable): Receive = {

    logger.info("become failed state")
    timers.startSingleTimer("reconnect", "reconnect", 2.second)

    return {
      case "reconnect" => {
        // timer 이벤트 아닌 다른 이벤틀 받고 , become 하는 경우는 cancel timer 후에 become 해야 함
        // timers.cancel("reconnect")

        // 여기는 timer event 받고 이동하는 거라 cancel 할 필요가 없음.
        context.become(retryState(retryCount + 1))
      }
      case req: MessageCallArgs => {
        if (req.requestTime < System.currentTimeMillis() - 3000) {
          req.sendResponse(
            sender(),
            CallResult(500, s"err = ${err.getMessage}")
          )
        } else {
          stash()
        }
      }
    }
  }

  override def receive: Receive = retryState(0)
}
