package restactor.service

import play.api.libs.json.{Json, Reads, Writes}

import scala.concurrent.Future

case class CallArgs( targetId : String,  first : String )

object CallArgs {
  implicit val reader : Reads[CallArgs] = Json.reads[CallArgs]
}
case class CallResult( status : Int, reason : String )

object CallResult {
  implicit val writer : Writes[CallResult] = Json.writes[CallResult]

}
trait RpcService {
  def call(  args : CallArgs ) : Future[CallResult]
}
