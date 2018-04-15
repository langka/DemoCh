package sse.xs.service

import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}

import sse.xs.msg.user.User

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Created by xusong on 2018/3/17.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
object DbService {

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

  val winStatement = generateStatement(winCount)
  val loseStatement = generateStatement(loseCount)


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
        val (win,lose) = winAndLoseOf(id)
        seq = seq :+ User(id,name,pwd,age,win.getOrElse(-1),lose.getOrElse(-1),description)
      }
      seq.headOption
    }
  }

   def winAndLoseOf(uid: Int) :(Option[Int],Option[Int])= {
    val w = winStatement flatMap  { x =>
      x.setInt(1, uid)
      val result = x.executeQuery()
      var seq: Seq[Int] = Seq()
      while (result.next()) {
        val win = result.getInt(1)
        seq = seq :+ win
      }
      seq.headOption
    }
    val l = loseStatement flatMap  { x =>
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
}

object DbInfo {

  val url = "jdbc:mysql://localhost:3306/chessdb"
  val user = "xusong"
  val pwd = "12345678"

  val addUser = "insert into user (user.name,user.password) values (?,?) ;"
  val getUser = "select user.name,user.password,user.uid,user.age,user.desc from user where name = ? and password = ?;"

  val updateUser = "update user values (?,?,?,?,?) where uid = ? ;"

  val insertGame = "insert into game (game.red,game.black,game.win,game.lose,game.steps) values (?,?,?,?,?);"

  val winCount = "select count(1) from game inner join user on game.win = user.uid where user.uid =?;"
  val loseCount = "select count(1) from game inner join user on game.lose = user.uid where user.uid = ? ;"

}
