package main

import akka.actor.{ActorSystem, Props}
import akka.routing.BalancingPool
import akka.util.Timeout
import sse.xs.actor.room.RoomManagerActor
import sse.xs.actor.user.{UserDBAcceptActor, UserManageActor}

import scala.concurrent.duration._

/**
  * Created by xusong on 2018/1/23.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
object Hello extends App {
  implicit val timeout = Timeout(5 seconds)
  implicit val exceutor = scala.concurrent.ExecutionContext.Implicits.global
  val system = ActorSystem(name = "nice")
  //USER
  val props = Props.create(classOf[UserDBAcceptActor]).withRouter(new BalancingPool(4))
  val userDBActor = system.actorOf(props, "userdb")
  val userManageActor = system.actorOf(Props(classOf[UserManageActor], userDBActor),"usermanager")
  val roomManageActor = system.actorOf(Props(classOf[RoomManagerActor]),"roommanager")
  println(userManageActor.path.toString)
  readLine()
}
