import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import services.{CosmosTxService, ICosmosTxService}

class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {
     bind(classOf[ICosmosTxService]).to(classOf[CosmosTxService])
  }
}
