package sse.xs.actor.user

import akka.actor.{Actor, ActorRef, Stash}
import sse.xs.actor.user.UserManageActor.{Request, Response}
import sse.xs.msg.CommonFailure
import sse.xs.msg.game.GetGameHistory
import sse.xs.service.DbService

/**
  * Created by xusong on 2018/3/17.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
//this actor is responsible for database checks,this is heavy work
//provide a single dispatcher
// accepts clients message directly

class UserDBAcceptActor extends Actor {

  import sse.xs.msg.user._

  val dbService = new DbService

  var manager: ActorRef = _
  var inited = false

  //成功返回给manageactor,否则直接向用户返回失败
  override def receive: Receive = ready

  def ready: Receive = {
    case Request(l: LoginRequest, id) =>
      dbService.getExistedUser(l.account, l.pwd) match {
        case Some(user) => sender() ! Response(LoginSuccess(user), id)
        case None => sender() ! Response(LoginFailure("Invalid account or password!"), id)
      }

    case Request(r: RegisterRequest, id) =>
      dbService.register(r.user, r.pwd) match {
        case Some(user) => sender() ! Response(RegisterSuccess(user), id)
        case None => sender() ! Response(RegisterFailure("Illegal or dulplicated account name!"), id)
      }

    case mpdify: ModifyU =>
      if (dbService.modifyUserInfo(mpdify.user)) {
        sender() ! ModifySuccess(mpdify.user)
      } else {
        sender() ! CommonFailure("无法修改数据库个人信息！")
      }

    //dbService.
    case getHistory: GetGameHistory =>
      val op=dbService.requireGameHistories(getHistory)
      if(op.isDefined){
        op foreach{g=>
          sender() ! g
        }
      }else{
        sender() ! CommonFailure("从数据库查询数据失败!")
      }
  }
}
