package restactor.impl.v2

import akka.actor.ActorSystem
import akka.util.Timeout
import play.api.Configuration
import restactor.service.{CallArgs, CallResult, DeviceDao, RpcService}
import restactor.utils.askUtil.AskUtil
import restactor.utils.{ResponseMesaageType, context}

import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

case class Requires@Inject()(config : Configuration, deviceDao : DeviceDao )

class RpcVersion2 @Inject()(requires : Requires, asys : ActorSystem) extends RpcService {
  val actor = asys.actorOf(MainActor.props(requires), "rpc-service-v2")

  implicit var dispatcher: ExecutionContext = asys.dispatcher

  override def call(args: CallArgs): Future[CallResult] = {
    implicit val timeout: Timeout = 10.seconds

    actor.askWith(MessageCallArgs( context.Background(), args))
  }
}

