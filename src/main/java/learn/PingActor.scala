package learn

import akka.actor.Actor

/**
  * Created by xusong on 2018/3/12.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
class PingActor extends Actor {
  override def receive = {
    case _ => println("receive")
      sender() ! "receive"
  }
}
