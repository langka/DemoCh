package sse.xs.msg.game

import sse.xs.msg.user.User

/**
  * Created by xusong on 2018/5/8.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
case class GameHistories(matches: List[SimpleMatch])

case class SimpleMatch(red: User, black: User, winner: Int, moves: String,gid:Int)

case class GetGameHistory(id: Int)
