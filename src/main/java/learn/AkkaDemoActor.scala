package learn

import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.event.Logging

import scala.collection.mutable

/**
  * Created by xusong on 2018/1/23.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
class AkkaDemoActor extends Actor {
  val map = new mutable.HashMap[String, Object]
  val log = Logging(context.system, this)

  override def receive: Receive = {

    case x => log.info("unexpected message " + x)
  }
}
