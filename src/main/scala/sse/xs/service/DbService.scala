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
        seq = seq :+ User(name, pwd)
      }
      seq.headOption
    }
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
}

object DbInfo {

  val url = "jdbc:mysql://localhost:3306/chessdb"
  val user = "xusong"
  val pwd = "12345678"

  val addUser = "insert into user (user.name,user.password) values (?,?) ;"
  val getUser = "select user.name,user.password from user where name = ? and password = ?;"

}
