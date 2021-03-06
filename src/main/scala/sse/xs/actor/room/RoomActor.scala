package sse.xs.actor.room

import akka.actor.{Actor, ActorRef, Props}
import sse.xs.actor.room.GameActor.GameEnded
import sse.xs.actor.room.RoomManagerActor.UpdateRoomInfo
import sse.xs.msg.CommonFailure
import sse.xs.msg.room._
import sse.xs.msg.user.User

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

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
    SwapRoom
   */


  val roomManger = context.parent
  //red and black
  var players: Array[Option[(ActorRef, User)]] = Array(None, None)
  players(0) = Some(master)

  var gameActor: ActorRef = _

  val talkMessages: ListBuffer[TalkMessage] = ListBuffer()

  val watchers: mutable.HashMap[ActorRef, User] = new mutable.HashMap[ActorRef, User]


  // get the current room state!
  private def getRoomInfo = {
    val users: Array[Option[User]] = players map {
      _.map {
        _._2
      }
    }
    val watching = watchers.values.toList

    RoomInfo(users, master._2, watching)
  }

  override def receive = waitingForAnother

  //initial state,wait for another one to join in room
  //there must be a vacant!
  def waitingForAnother: Receive = messageDispatcher orElse
    swapDispatcher orElse watcherDispatcher orElse {
    case EnterRoom(user: User) =>
      if (players(0).isEmpty) {
        players(0) = Some((sender(), user))
        val info = getRoomInfo
        sender() ! EnterRoomSuccess(info, token)
        players(1) foreach { x =>
          x._1 ! NewUserEnter(info)
        }
        notifyAllWatchers(NewUserEnter(info))
      } else {
        players(1) = Some(sender(), user)
        val info = getRoomInfo
        sender() ! EnterRoomSuccess(info, token)
        players(0) foreach {
          x =>
            x._1 ! NewUserEnter(info)
        }
        notifyAllWatchers(NewUserEnter(info))
      }

      roomManger ! UpdateRoomInfo(getRoomInfo, token)
      //
      context.become(waitToStart)
    case LeaveRoom(user) =>
      findUser(user) match {
        case -1=>
          watchers.remove(sender())
          val o = OtherLeaveRoom(getRoomInfo)
          notifyAllPlayers(o)
          notifyAllWatchers(o)

        case _=>
          sender() ! LeaveRoomSuccess
          roomManger ! DestroyRoom(token)
          context.stop(self)
      }

  }

  def waitToStart: Receive = messageDispatcher orElse
    swapDispatcher orElse
    rejectEnter("房间已满") orElse watcherDispatcher orElse {
    case StartGame =>
      notifyAllPlayers(GameStarted(getRoomInfo))
      notifyAllWatchers(GameStarted(getRoomInfo))
      gameActor = context.actorOf(Props(classOf[GameActor], self, players(0).get._1, players(1).get._1,
        players(0).get._2.id, players(1).get._2.id,watchers))
      context.become(gameStarted)
    case LeaveRoom(user) =>
      //有人离开
      findUser(user) match {
        case -1 =>
        //观众
          watchers.remove(sender())
          val o = OtherLeaveRoom(getRoomInfo)
          notifyAllPlayers(o)
          notifyAllWatchers(o)

        case i: Int =>
          val other = if (i == 0) 1 else 0
          sender() ! LeaveRoomSuccess

          players(i) = None
          master = players(other).get
          players(other) foreach {
            _._1 ! OtherLeaveRoom(getRoomInfo)
          }
          notifyAllWatchers(OtherLeaveRoom(getRoomInfo))
          context.become(waitingForAnother)
      }
      roomManger ! UpdateRoomInfo(getRoomInfo, token)
  }

  def gameStarted: Receive = messageDispatcher orElse {
    case m: Move => gameActor forward m
    case e: EndGame => gameActor forward e
    case Surrender => gameActor forward Surrender
    case GameEnded => context.become(waitToStart)

    case e:EnterRoom =>sender() ! CommonFailure("游戏已开始!")
    case w:WatchGame =>sender() ! CommonFailure("游戏已开始!")
  }


  def watcherDispatcher: Receive = {
    case w: WatchGame =>
      val previous = getActors
      watchers.put(sender(), w.user)
      val info2 = getRoomInfo
      sender() ! EnterRoomSuccess(info2,token)
      val enterInfo = NewUserEnter(info2)
      previous.foreach( _ ! enterInfo)

    case WatcherLeave =>
      watchers.remove(sender())
      notifyAllPlayers(OtherLeaveRoom(getRoomInfo))
      notifyAllWatchers(OtherLeaveRoom(getRoomInfo))
  }

  def getActors:List[ActorRef] = {
    val p=players.filter(_.isDefined).map(_.get).map(_._1).toList
    val w = watchers.keys.toList
    p ++ w
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
      notifyAllWatchers(msg)
      talkMessages.append(msg)
  }

  def swapDispatcher: Receive = {
    case SwapRoom =>
      val temp = players(0)
      players(0) = players(1)
      players(1) = temp
      notifyAllPlayers(SwapSuccess(getRoomInfo))
      notifyAllWatchers(SwapSuccess(getRoomInfo))
  }


  def rejectEnter(msg: String): Receive = {
    case e: EnterRoom => sender() ! CommonFailure(msg)
  }

  def findUser(user: User) = {
    if (players(0).exists(_._2.name == user.name)) 0
    else if (players(1).exists(_._2.name == user.name)) 1
    else -1
  }

  def notifyAllPlayers(msg: Any): Unit = {
    players foreach { p => p.foreach(_._1 ! msg) }
  }

  def notifyAllWatchers(msg: Any): Unit = {
    watchers foreach {
      p =>
        p._1 ! msg
    }
  }

}

object RoomActor {
  def props(token: Long, owner: (ActorRef, User)): Props = {
    Props(classOf[RoomActor], token, owner)
  }
}

