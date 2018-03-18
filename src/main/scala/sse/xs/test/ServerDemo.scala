package sse.xs.test

import akka.actor.{ActorSystem, Props}
import sse.xs.actor.room.RoomManagerActor

/**
  * Created by xusong on 2018/3/18.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
object ServerDemo extends App{
  val system = ActorSystem()
  val ac=system.actorOf(Props[RoomManagerActor],"roommanager")
  println(ac.path.toString)
}
