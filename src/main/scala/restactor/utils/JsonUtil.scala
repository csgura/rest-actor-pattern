package restactor.utils

import play.api.libs.json.{JsArray, JsBoolean, JsNull, JsNumber, JsObject, JsString, JsValue}

object JsonUtil {
  def jsValueToObj( jsvalue : JsValue ) : Any = {
    jsvalue match {
      case a  : JsObject => jsObjectToMap(a)
      case a : JsArray => a.value.map(jsValueToObj)

      // case a : JsNumber => if(a.value.scale == 0) a.value.toLong else a.value.toDouble    =>   LONG_MAX 보다 큰 경우 처리 불만
      // case a : JsNumber => Try(a.value.toLongExact).getOrElse(a.value.toDouble)  => Exception이 발생하는 것이 불만
      case a : JsNumber => // => 100.0 과 같은 것도 long 으로 변환 가능
        val ret = a.value.toDouble
        if(ret == ret.toLong)
          ret.toLong
        else {
          ret
        }
      case a : JsBoolean => a.value
      case a : JsString => a.value
      case JsNull => null
    }
  }


  def jsObjectToMap( jsobj : JsObject ) : Map[String , Any] = {
    jsobj.value.view.mapValues{
      case a : JsObject => jsObjectToMap(a)
      case a => jsValueToObj(a)
    }.toMap
  }


  def jsValueToMap( jsvalue : JsValue ) : Map[String , Any] = {
    jsvalue match {
      case a : JsObject => jsObjectToMap(a)
      case _ => Map()
    }
  }

  def jsValueToSeq( jsvalue : JsValue ) : Seq[Any] = {
    jsvalue match {
      case a : JsArray => a.value.map(jsValueToObj).toSeq
      case _ => Seq()
    }
  }
}