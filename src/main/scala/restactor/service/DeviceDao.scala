package restactor.service

import scala.concurrent.Future

trait DeviceDao {
  def storeInfo(di: DeviceInfo) : Future[Unit]


  def loadInfo(id : String) : Future[DeviceInfo]
}

case class DeviceInfo(capa : String, num : Int) {
  def incrNum() : DeviceInfo = {
    DeviceInfo(capa, num+1)
  }
}
