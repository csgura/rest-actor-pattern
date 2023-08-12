package restactor.impl.mocks

import play.api.Logging
import restactor.service.{DeviceDao, DeviceInfo}

import javax.inject.Singleton
import scala.concurrent.Future

@Singleton
class MockDeviceDao extends DeviceDao with Logging {
  override def storeInfo(di: DeviceInfo): Future[Unit] = {
    logger.info(s"store Info $di")
    Future.successful(())
  }

  override def loadInfo(id: String): Future[DeviceInfo] = {
    logger.info(s"load Info $id")

    if (id.equals("none")) {
      Future.failed(new RuntimeException("no connection"))
    }

    Future.successful(DeviceInfo("10", 0))

  }
}
