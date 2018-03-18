package sse.xs.actor.user

import akka.actor.{Actor, ActorRef}
import sse.xs.service.DbService._

/**
  * Created by xusong on 2018/3/17.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
//this actor is responsible for database checks,this is heavy work
//provide a single dispatcher
// accepts clients message directly

class UserDBAcceptActor(target: ActorRef) extends Actor {

  import sse.xs.msg.user._

  val manager = target

  override def receive: Receive = {
    case LoginRequest(account, pwd) =>
      getExistedUser(account, pwd) match {
        case Some(user) => manager forward LoginSuccess(user)
        case None => sender() ! LoginFailure("Invalid account or password!")
      }

    case RegisterRequest(account, pwd) =>
      register(account, pwd) match {
        case Some(user) => manager forward RegisterSuccess(user)
        case None => sender() ! RegisterFailure("Illegal or dulplicated account name!")
      }
  }
}
