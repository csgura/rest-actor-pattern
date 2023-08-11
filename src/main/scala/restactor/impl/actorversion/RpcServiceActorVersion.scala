package restactor.impl.actorversion

import akka.actor.ActorSystem
import akka.util.Timeout
import play.api.Configuration
import restactor.service.{CallArgs, CallResult, RpcService, DeviceDao}
import restactor.utils.askUtil.AskUtil

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.duration.DurationInt


case class Requires@Inject()(config : Configuration, deviceDao : DeviceDao )

class RpcServiceActorVersion @Inject()(requires : Requires, asys : ActorSystem) extends RpcService{


  val actor = asys.actorOf(MainActor.props(requires) , "rpc-service")
  implicit var dispatcher: ExecutionContext = asys.dispatcher

  override def call(args: CallArgs): Future[CallResult] = {

    implicit val timeout : Timeout = 10.seconds

    actor.askWith( MessageCallArgs(System.currentTimeMillis(),  args))

  }
}
