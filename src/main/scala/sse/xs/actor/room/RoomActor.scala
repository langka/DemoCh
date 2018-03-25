package sse.xs.actor.room

import akka.actor.{Actor, ActorRef, Props}
import sse.xs.actor.room.RoomManagerActor.UpdateRoomInfo
import sse.xs.msg.CommonFailure
import sse.xs.msg.room._
import sse.xs.msg.user.{NoBody, User}

/**
  * Created by xusong on 2018/3/17.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  * this respresents a single room in the server
  */
class RoomActor(token: Long, var master: (ActorRef, User)) extends Actor {

  /*
    accepted messages:
    EnterRoom
    LeaveRoom
    StartGame


   */


  val roomManger = context.parent
  //red and black
  var players: Array[Option[(ActorRef, User)]] = Array(None, None)
  players(0) = Some(master)

  var gameActor: ActorRef = _


  // get the current room state!
  private def getRoomInfo = {
    val users: Array[Option[User]] = players map {
      _.map {
        _._2
      }
    }
    RoomInfo(users, master._2)
  }

  override def receive = waitingForAnother

  //initial state,wait for another one to join in room
  //there must be a vacant!
  def waitingForAnother: Receive = messageDispatcher orElse {
    case EnterRoom(user: User) =>
      if (players(0).isEmpty) {
        players(0) = Some((sender(), user))
        val info = getRoomInfo
        sender() ! EnterRoomSuccess(info)
        players(1) foreach {
          _._1 ! NewUserEnter(info)
        }
      } else {
        players(1) = Some(sender(), user)
        val info = getRoomInfo
        sender() ! EnterRoomSuccess(info)
        players(0) foreach {
          _._1 ! NewUserEnter(info)
        }
      }
      roomManger ! UpdateRoomInfo(getRoomInfo,token)
      //
      context.become(waitToStart)
    case LeaveRoom(user) =>
      sender() ! LeaveRoomSuccess
      roomManger ! DestroyRoom(token)
      context.become(waitTobeKilled)
  }

  def waitToStart: Receive = messageDispatcher orElse
    rejectEnter("房间已满") orElse {
    case StartGame =>
      notifyAllUser(GameStarted(getRoomInfo))
      gameActor = context.actorOf(Props(classOf[GameActor], players(0).get._1, players(1).get._1))
      context.become(gameStarted)
    case LeaveRoom(user) =>
      //有人离开
      findUser(user) match {
        case -1 =>
        //do nothing
        case i: Int =>
          val other = if (i == 0) 1 else 0
          sender() ! LeaveRoomSuccess
          players(other) foreach {
            _._1 ! OtherLeaveRoom
          }
          players(i) = None
          master = players(other).get
          context.become(waitingForAnother)
      }
      roomManger ! UpdateRoomInfo(getRoomInfo,token)
  }

  def gameStarted: Receive = messageDispatcher orElse {
    case m: Move => gameActor forward m
  }

  //the room is invalid now
  def waitTobeKilled: Receive = {
    case _ => sender() ! CommonFailure("房间链接已失效！")
  }

  //转发消息的逻辑
  def messageDispatcher: Receive = {
    case msg: TalkMessage => players foreach {
      _.foreach {
        _._1 ! msg
      }
    }
  }

  def rejectEnter(msg: String): Receive = {
    case e: EnterRoom => sender() ! CommonFailure(msg)
  }

  def findUser(user: User) = {
    if (players(0).exists(_._2.name == user.name)) 0
    else if (players(1).exists(_._2.name == user.name)) 1
    else -1
  }

  def notifyAllUser(msg: Any): Unit = {
    players foreach { p => p.foreach(_._1 ! msg) }
  }

}

object RoomActor {
  def props(token: Long, owner:(ActorRef, User)): Props = {
    Props(classOf[RoomActor], token, owner)
  }
}

