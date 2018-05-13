package sse.xs.actor.user

import akka.actor.{Actor, ActorIdentity, ActorRef, Identify}
import sse.xs.actor.user.UserManageActor.{Request, Response}
import sse.xs.msg.game.GetGameHistory
import sse.xs.msg.room.{InviteMessage, TalkMessage}
import sse.xs.msg.user._

import scala.collection.mutable

/**
  * Created by xusong on 2018/3/17.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  *
  * This should be a singleton actor!
  */
class UserManageActor(dbActor: ActorRef) extends Actor {


  var userToken = 0L
  var tokenMap = new mutable.HashMap[Long, User]
  var userMap = new mutable.HashMap[String, ActorRef]

  var requestCount = 0L
  var requestMap = new mutable.HashMap[Long, ActorRef]

  //val selection  =context.actorSelection("akka.tcp://default@10.209.8.205:2552/user/um")
  //record the info in map
  def cachedInMap(user: User)(s: ActorRef) = {
    userToken += 1
    tokenMap.put(userToken, user)
    userMap.put(user.name, s)
  }

  override def receive = {
    case "HELLO" =>
      sender() ! "OJBK"
    case l: LoginRequest =>
      println("sender:" + sender().path)
      requestCount += 1
      dbActor ! Request(l, requestCount)
      requestMap.put(requestCount, sender())
      println("received login request")
    case r: RegisterRequest =>
      requestCount += 1
      dbActor ! Request(r, requestCount)
      requestMap.put(requestCount, sender())
      println("received register request")
    case Response(l: LoginSuccess, id) =>
      val requester = requestMap.remove(id)
      requester.foreach(cachedInMap(l.user))
      requester.foreach(_ ! l)
      println("received login success")
    case Response(r: RegisterSuccess, id) =>
      val requester = requestMap.remove(id)
      requester.foreach(cachedInMap(r.user))
      requester.foreach(_ ! r)
      println("received reg success")
    case Response(l: LoginFailure, id) =>
      val requester = requestMap.remove(id)
      requester foreach (_ ! l)
      println("received log failure")
    case Response(r: RegisterFailure, id) =>
      requestMap.remove(id) foreach (_ ! r)

    //直接转发的message,后续可能针对消息的不同种类作不同处理
    case t: TalkMessage =>
      notifyAllUser(t)
    case i: InviteMessage =>
      notifyAllUser(i)


    case modify: ModifyU =>
      dbActor forward modify

    case get: GetGameHistory =>
      dbActor forward get

  }

  private def getAllUserRef = {
    userMap.values
  }

  def notifyAllUser(x: Any): Unit = {
    getAllUserRef foreach { a =>
      a ! x
    }
  }
}

object UserManageActor {

  case class Request(request: Any, id: Long)

  case class Response(resp: Any, id: Long)

}
