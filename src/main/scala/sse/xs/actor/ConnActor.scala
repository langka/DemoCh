package sse.xs.actor

import akka.actor.Actor
import sse.xs.msg.conn._

/**
  * Created by xusong on 2018/3/17.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
class ConnActor extends Actor {
  var token = 1L

  override def receive = {
    case ConnRequest(id) =>
      token += 1
      sender() ! ConnResp(id, token)
  }
}
