package sse.xs.actor.room

import akka.actor.{Actor, ActorRef}
import sse.xs.actor.room.RoomManagerActor.UpdateRoomInfo
import sse.xs.msg.room._

import scala.collection.mutable

/**
  * Created by xusong on 2018/3/17.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  *
  * Accepted Messages are:
  * from client:
  *   GetAllRooms
  *   CreateRoom
  *
  * from local:
  *   DestroyRoom
  *   UpdateRoomInfo
  *
  *
  */


class RoomManagerActor extends Actor {


  var roomId = 0L
  val rooms = new mutable.HashMap[Long, ActorRef]()
  val infos = new mutable.HashMap[Long, RoomInfo]()

  override def receive = {

    case "HELLO" =>
      sender() ! "OJBK"

    case GetAllRooms =>
      sender() ! RoomSearchResponse(infos.toArray)

    case CreateRoom(user) =>
      roomId += 1
      val room = context.actorOf(RoomActor.props(roomId, (sender(), user)), "room" + roomId)
      rooms.put(roomId, room)
      val players = Array(Some(user), None)
      infos.put(roomId, RoomInfo(players, user))
      sender() ! CreateSuccess(roomId)

    case DestroyRoom(id) =>
      rooms.get(id) foreach context.stop
      rooms.remove(id)
    case UpdateRoomInfo(info, id) =>
      infos.put(id, info)
  }
}

object RoomManagerActor {

  case class UpdateRoomInfo(info: RoomInfo, id: Long)

}
