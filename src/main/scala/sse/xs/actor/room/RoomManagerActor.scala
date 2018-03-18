package sse.xs.actor.room

import akka.actor.{Actor, ActorRef}
import sse.xs.msg.room._

import scala.collection.mutable

/**
  * Created by xusong on 2018/3/17.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
class RoomManagerActor extends Actor {


  var roomId = 0L
  var rooms = new mutable.HashMap[Long, ActorRef]()

  override def receive = {

    case GetAllRooms =>
      sender() ! RoomSearchResponse(rooms.keys.toList)

    case CreateRoom(user) =>
      roomId += 1
      val room = context.actorOf(RoomActor.props(roomId, (sender(), user)), "room" + roomId)
      rooms.put(roomId, room)
      sender() ! CreateSuccess(roomId)

    case DestroyRoom(id) =>
      rooms.get(id) foreach context.stop
      rooms.remove(id)

  }
}
