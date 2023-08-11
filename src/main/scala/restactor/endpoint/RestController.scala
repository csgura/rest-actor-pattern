package restactor.endpoint

import akka.actor.ActorSystem
import play.api.libs.json.Json
import play.api.{Configuration, Logging}
import play.api.mvc.{AbstractController, ControllerComponents}
import restactor.service.{CallArgs, RpcService}

import javax.inject.Inject
import scala.concurrent.ExecutionContext



class RestController @Inject()(config : Configuration , service : RpcService, cc: ControllerComponents)(implicit ec : ExecutionContext) extends AbstractController(cc) with Logging {


  def callRpc = Action.async(parse.json[CallArgs]) {
    request => {
      service.call(request.body)
        .map(Json.toJson(_))
        .map(Ok(_))
    }
  }
}
