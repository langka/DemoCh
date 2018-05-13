package sse.xs.service

import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}

import sse.xs.msg.game.{GameHistories, GetGameHistory, SimpleMatch}
import sse.xs.msg.user.User

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Created by xusong on 2018/3/17.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
class DbService {

  import DbInfo._


  val conn: Option[Connection] = {
    try {
      println("trying to connect to mysql")
      Class.forName("com.mysql.jdbc.Driver")
      Some(DriverManager.getConnection(url, user, pwd))
    } catch {
      case e: Exception =>
        println(e)
        println("connection to mysql failure")
        None
    }
  }


  type OPS = Option[PreparedStatement]

  val addUserStatement: OPS = generateStatement(addUser)

  val getUserStatement: OPS = generateStatement(getUser)

  val addGameStatement: OPS = generateStatement(insertGame)

  val updateUserStatement: OPS = generateStatement(updateUser)


  val winStatement = generateStatement(winCount)
  val loseStatement = generateStatement(loseCount)

  val userMatchStatement = generateStatement(userMatch)


  def generateStatement(str: String): Option[PreparedStatement] = conn map (_.prepareStatement(str))


  def getExistedUser(account: String, pwd: String): Option[User] = {
    getUserStatement flatMap { x =>
      x.setString(1, account)
      x.setString(2, pwd)
      val result: ResultSet = x.executeQuery()
      var seq: Seq[User] = Seq()
      while (result.next()) {
        val name = result.getString(1)
        val pwd = result.getString(2)
        val id = result.getInt(3)
        val age = result.getInt(4)
        val description = result.getString(5)
        val (win, lose) = winAndLoseOf(id)
        seq = seq :+ User(id, name, pwd, age, win.getOrElse(-1), lose.getOrElse(-1), description)
      }
      seq.headOption
    }
  }

  def winAndLoseOf(uid: Int): (Option[Int], Option[Int]) = {
    val w = winStatement flatMap { x =>
      x.setInt(1, uid)
      val result = x.executeQuery()
      var seq: Seq[Int] = Seq()
      while (result.next()) {
        val win = result.getInt(1)
        seq = seq :+ win
      }
      seq.headOption
    }
    val l = loseStatement flatMap { x =>
      x.setInt(1, uid)
      val result = x.executeQuery()
      var seq: Seq[Int] = Seq()
      while (result.next()) {
        val win = result.getInt(1)
        seq = seq :+ win
      }
      seq.headOption
    }
    (w, l)
  }


  def register(account: String, pwd: String): Option[User] = {
    addUserStatement flatMap { x =>
      x.setString(1, account)
      x.setString(2, pwd)
      try {
        if (x.executeUpdate() == 1) {
          Some(User(account, pwd))
        }
        else None
      } catch {
        case e: Exception => None
      }
    }
  }

  def saveGame(r: Int, b: Int, win: Int, lose: Int, steps: String): Boolean = {
    val result = addGameStatement map { x =>
      x.setInt(1, r)
      x.setInt(2, b)
      x.setInt(3, win)
      x.setInt(4, lose)
      x.setString(5, steps)
      try {
        if (x.executeUpdate() == 1)
          true
        else
          false
      }
      catch {
        case e: Exception =>
          false
      }
    }
    result.exists(x => x)
  }


  def modifyUserInfo(u: User): Boolean = {
    updateUserStatement exists { stmt =>
      stmt.clearParameters()
      stmt.setString(1, u.description)
      stmt.setInt(2, u.age)
      stmt.setInt(3, u.id)
      stmt.setString(4, u.name)
      try {
        if (stmt.executeUpdate() == 1)
          return true
        else
          return false
      }
      catch {
        case e: Exception =>
          val d = e
          return false
      }

      true

    }
  }

  def requireGameHistories(g: GetGameHistory): Option[GameHistories] = {
    userMatchStatement map { stmt =>
      stmt.clearParameters()
      stmt.setInt(1, g.id)
      stmt.setInt(2, g.id)
      val result = stmt.executeQuery()
      var list: List[SimpleMatch] = List()
      while (result.next()) {
        val gid = result.getInt(1)
        val steps = result.getString(2)
        val win = result.getInt(3)
        val rid = result.getInt(4)
        val rname = result.getString(5)
        val bid = result.getInt(6)
        val bname = result.getString(7)
        val u1 = User(rname, rid)
        val u2 = User(bname, bid)
        val simpleMatch = SimpleMatch(u1, u2, win, steps, gid)
        list = list :+ simpleMatch
      }

      GameHistories(list)

    }

  }

}

object DbInfo {

  val url = "jdbc:mysql://localhost:3306/chessdb"
  val user = "xusong"
  val pwd = "12345678"

  val addUser = "insert into user (user.name,user.password) values (?,?) ;"
  val getUser = "select user.name,user.password,user.uid,user.age,user.description from user where name = ? and password = ?;"

  val updateUser = "update user set description = ?,age = ? where uid = ? or name =?;"

  val insertGame = "insert into game (game.red,game.black,game.win,game.lose,game.steps) values (?,?,?,?,?);"

  val winCount = "select count(1) from game inner join user on game.win = user.uid where user.uid =?;"
  val loseCount = "select count(1) from game inner join user on game.lose = user.uid where user.uid = ? ;"

  val userMatch = "select game.gid,game.steps,game.win,u1.uid,u1.name,u2.uid,u2.name from game join user as u1 " +
    "on game.red=u1.uid join user as u2 on game.black=u2.uid where game.red=? or game.black=?"

}
