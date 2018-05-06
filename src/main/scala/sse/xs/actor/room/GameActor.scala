package sse.xs.actor.room

import akka.actor.{Actor, ActorRef}
import sse.xs.actor.room.GameActor.GameEnded
import sse.xs.actor.room.GameDbActor.SaveGame
import sse.xs.msg.CommonFailure
import sse.xs.msg.room._
import sse.xs.util.StrUtil

import scala.collection.mutable.ArrayBuffer

/**
  * Created by xusong on 2018/3/18.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
class GameActor(room: ActorRef, red: ActorRef, black: ActorRef, rid: Int, bid: Int) extends Actor {
  //0 red,1 black
  var redTurn = true
  val steps: ArrayBuffer[Move] = new ArrayBuffer[Move]
  var stepCount = 0

  //gamedb actor
  private val gameDbActor = context.actorSelection("/user/gamedb")

  override def receive = redTurnToMove


  val illegalMove: Receive = {
    case _: Move =>
      sender() ! CommonFailure("Not your turn to move!")
  }

  def redTurnToMove: Receive = {
    val rule: Receive = {
      case move: Move =>
        if (sender() == red) {
          steps.append(move)
          redTurn = false
          stepCount = stepCount + 1
          sender() ! MoveSuccess
          black ! OtherMove(move.from, move.to)
          context.become(blackTurnToMove)
        }
    }
    rule orElse illegalMove
  }

  def blackTurnToMove: Receive = {
    val rule: Receive = {
      case move: Move =>
        if (sender() == black) {
          steps.append(move)
          redTurn = true
          stepCount = stepCount + 1
          sender() ! MoveSuccess
          red ! OtherMove(move.from, move.to)
          context.become(redTurnToMove)
        }
    }
    rule orElse illegalMove
  }

  def endGame: Receive = {
    case Surrender =>
      val redWin = if (sender() == red) false else true
      val win = if (sender() == red) bid else rid
      val lose = if (sender() == red) rid else bid
      red ! EndGame(redWin)
      black ! EndGame(redWin)
      gameDbActor ! SaveGame(rid, bid, win, lose, StrUtil.getStepsAsString(steps))
      room ! GameEnded
      context.stop(self)
    case EndGame(redWin) =>
      if (redWin)
        gameDbActor ! SaveGame(rid, bid, rid, bid, StrUtil.getStepsAsString(steps))
      else
        gameDbActor ! SaveGame(rid, bid, bid, rid, StrUtil.getStepsAsString(steps))
      //现在游戏结束由客户端自行判定，与服务器端无关,服务器端不需要通知客户端
      //this.red ! EndGame(redWin)
      // this.black ! EndGame(redWin)
      room ! GameEnded
      context.stop(self)
  }

  def ended: Receive = {
    case _ => sender() ! CommonFailure("Game ended!")
  }


}

object GameActor {

  case object GameEnded

}