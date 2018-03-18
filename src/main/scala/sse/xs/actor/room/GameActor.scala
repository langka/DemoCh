package sse.xs.actor.room

import akka.actor.{Actor, ActorRef}
import sse.xs.msg.CommonFailure
import sse.xs.msg.room.{Move, MoveSuccess, OtherMove}

/**
  * Created by xusong on 2018/3/18.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
class GameActor(red: ActorRef, black: ActorRef) extends Actor {
  //0 red,1 black
  var redTurn = true
  var steps: List[Move] = List()
  var stepCount = 0

  override def receive = redTurnToMove

  val illegalMove: Receive = {
    case _: Move =>
      sender() ! CommonFailure("Not your turn to move!")
  }

  def redTurnToMove: Receive = {
    val rule: Receive = {
      case move: Move =>
        if (sender() == red) {
          steps = move +: steps
          redTurn = false
          stepCount = stepCount + 1
          sender() ! MoveSuccess
          black ! OtherMove(move.from, move.to)
        }
    }
    rule orElse illegalMove
  }

  def blackTurnToMove: Receive = {
    val rule: Receive = {
      case move: Move =>
        if (sender() == red) {
          steps = move +: steps
          redTurn = true
          stepCount = stepCount + 1
          sender() ! MoveSuccess
          red ! OtherMove(move.from, move.to)
        }
    }
    rule orElse illegalMove
  }

}
