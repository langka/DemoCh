/**
  * Created by xusong on 2018/3/18.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */

import akka.actor.{Actor, ActorSystem}
import akka.testkit.TestActorRef
import akka.util.Timeout

import scala.concurrent.duration._
import org.scalatest.{FunSpecLike, Matchers}
import sse.xs.actor.room.{GameActor, RoomManagerActor}
import sse.xs.msg.room._
import sse.xs.msg.user.User
import akka.pattern._

import scala.concurrent.Await

class RoomActorTest extends FunSpecLike with Matchers {

  class PrintActor(user: User) extends Actor {
    override def receive = {
      case e => println(user.name + " received : " + e)
    }
  }

  implicit val system = ActorSystem()
  implicit val timeout = Timeout(5 seconds)

  val manager = TestActorRef(new RoomManagerActor)

  describe("create a roommanager") {
    it("get room list") {
      val user1 = User("xusong", "xs")
      val user2 = User("gaoyuan", "gy")
      val user3 = User("luanyk", "lyk")
      val t1 = TestActorRef(new PrintActor(user1))
      val t2 = TestActorRef(new PrintActor(user2))
      val t3 = TestActorRef(new PrintActor(user3))
      manager.tell(CreateRoom(user1), t1)
      manager.tell(CreateRoom(user2), t2)
      manager.tell(GetAllRooms, t3)
      val child = manager.getSingleChild("room" + 1)
      child.tell(EnterRoom(user3), t3)
      manager.tell(EnterRoom(user2), t2)

    }
    it("print serialised"){

    }
  }

}


