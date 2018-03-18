package sse.xs.actor.user

import akka.actor.{Actor, ActorRef}
import sse.xs.msg.user.{LoginSuccess, RegisterSuccess, User}

import scala.collection.mutable

/**
  * Created by xusong on 2018/3/17.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  *
  * This should be a singleton actor!
  */
class UserManageActor extends Actor {


  var token = 0L
  var tokenMap = new mutable.HashMap[Long, User]()
  var userMap = new mutable.HashMap[String, ActorRef]()

  //record the info in map
  def cachedInMap(user: User) = {
    token += 1
    tokenMap.put(token, user)
    userMap.put(user.name, sender())
  }

  override def receive = {
    case ls: LoginSuccess =>
      cachedInMap(ls.user)
      sender() ! ls
    case rs: RegisterSuccess =>
      cachedInMap(rs.user)
      sender() ! rs
  }
}
