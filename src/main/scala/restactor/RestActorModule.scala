package restactor

import com.google.inject.AbstractModule
import restactor.impl.actorversion.RpcServiceActorVersion
import restactor.impl.mocks.MockDeviceDao
import restactor.impl.v2.RpcVersion2
import restactor.service.{DeviceDao, RpcService}

class RestActorModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[RpcService]).to(classOf[RpcServiceActorVersion]).in(classOf[javax.inject.Singleton])
    bind(classOf[DeviceDao]).to(classOf[MockDeviceDao]).in(classOf[javax.inject.Singleton])
  }
}


class RestActorModuleV2 extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[RpcService]).to(classOf[RpcVersion2]).in(classOf[javax.inject.Singleton])
    bind(classOf[DeviceDao]).to(classOf[MockDeviceDao]).in(classOf[javax.inject.Singleton])
  }
}
