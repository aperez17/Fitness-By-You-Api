package security

import javax.inject.Inject
import play.api.mvc.{ AnyContent, Request }
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

import model.User
import play.api.mvc.{Result}
import play.api.libs.json.Json

trait Authenticator {
  
  def authenticate(request: Request[AnyContent]): Future[Result]
  
  def findOneByUsername(userName: String): Option[User]
}