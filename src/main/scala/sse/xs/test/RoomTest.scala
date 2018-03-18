package sse.xs.test

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.TestActorRef
import akka.util.Timeout
import sse.xs.actor.room.RoomManagerActor
import sse.xs.msg.room.{CreateRoom, EnterRoom, GetAllRooms, LeaveRoom}
import sse.xs.msg.user.User

import scala.concurrent.Await

/**
  * Created by xusong on 2018/3/18.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */

import scala.concurrent.duration._

object RoomTest extends App {
  val system = ActorSystem()
  implicit val timeout = Timeout(5 seconds)
  val path = "akka.tcp://default@10.209.8.196:2553/user/roommanager"

  val manager:ActorRef = Await.result(system.actorSelection(path).
    resolveOne(),5 seconds)
  val user1 = User("xusong", "xs")
  val user2 = User("gaoyuan", "gy")
  val user3 = User("luanyk", "lyk")
  val t1 = system.actorOf(Props(classOf[PrintActor],user1))
  val t2 = system.actorOf(Props(classOf[PrintActor],user2))
  val t3 = system.actorOf(Props(classOf[PrintActor],user3))
  manager.tell(CreateRoom(user1), t1)
  manager.tell(CreateRoom(user2), t2)
  manager.tell(GetAllRooms, t3)
  val p2 = path+"/room1"
  val child =  Await.result(system.actorSelection(p2).
    resolveOne(), 5 seconds)
  Thread.sleep(2000)
  child.tell(EnterRoom(user3), t3)
  child.tell(EnterRoom(user2), t2)
  child.tell(LeaveRoom(user1), t1)
  child.tell(EnterRoom(user2), t2)
}

class PrintActor(user: User) extends Actor {
  override def receive = {
    case e => println(user.name + " received : " + e)
  }
}