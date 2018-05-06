package sse.xs.actor.room

import akka.actor.Actor
import sse.xs.actor.room.GameDbActor.SaveGame
import sse.xs.service.DbService

/**
  * Created by xusong on 2018/4/16.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
class GameDbActor extends Actor {
  val dbService = new DbService

  override def receive: Receive = {
    case s: SaveGame =>
      dbService.saveGame(s.rid, s.bid, s.win, s.lose, s.steps)
  }
}

object GameDbActor {

  case class SaveGame(rid: Int, bid: Int, win: Int, lose: Int, steps: String)

}
