package restactor.impl.v2

import akka.actor.Actor.Receive
import play.api.Logging
import restactor.service.{CallResult, DeviceInfo}
import restactor.utils.ActorState

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt

case class ConnectionError(err : Throwable)

class OkState(actor : ChildActor, di : DeviceInfo) extends ActorState with Logging {
  implicit val dispatcher : ExecutionContext = actor.dispatcher

  override def onEnter(): Unit = {
    actor.context.setReceiveTimeout(5.second)
    actor.requires.deviceDao.storeInfo(di)
  }

  override def postStop(): Unit = {
    actor.requires.deviceDao.storeInfo(di)
  }

  def callRpc(di: DeviceInfo, callArgs: MessageCallArgs): Future[CallResult] = {
    Future.successful(CallResult(di.num, "ok"))
  }

  override def receive: Receive = {
    case rpcCall: MessageCallArgs => {

      val returnPath = actor.sender()
      val rfuture = callRpc(di, rpcCall)
      rfuture.foreach(res => rpcCall.sendResponse(returnPath, res))
      rfuture.failed.foreach(err => {
        rpcCall.sendError(returnPath, err)

        if (err.isInstanceOf[RuntimeException]) {
          actor.self ! ConnectionError(err)
        }
      })

      logger.info(
        s"call incr num : ${di.num},  rpc : args : ${rpcCall.callArgs}"
      )

      rpcCall.sendResponse(actor.sender(), CallResult(di.num, "ok"))
      actor.become(new OkState(actor, di.incrNum()))
    }
    case err: ConnectionError => {
      actor.become(new FailedState(actor,0, err.err))
    }
    case akka.actor.ReceiveTimeout => {
      actor.context.stop(actor.self);
    }
  }
}
