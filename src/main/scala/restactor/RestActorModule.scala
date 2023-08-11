package restactor

import com.google.inject.AbstractModule
import restactor.impl.actorversion.RpcServiceActorVersion
import restactor.impl.mocks.MockDeviceDao
import restactor.service.{DeviceDao, RpcService}

class RestActorModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[RpcService]).to(classOf[RpcServiceActorVersion]).asEagerSingleton()
    bind(classOf[DeviceDao]).to(classOf[MockDeviceDao]).asEagerSingleton()
  }
}
