package sse.xs.test

import sse.xs.msg.user.User
import sse.xs.service.DbService.{getExistedUser, register}

/**
  * Created by xusong on 2018/3/18.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
object Lazytest extends App {
  val u = User("xusong2", "123")
  val f: Int => Any => Unit = { x =>
    y =>
      println(x + "" + y)
  }
  getExistedUser(u.name, u.pwd) foreach f(0)
  register(u.name, u.pwd) foreach  f(1)
  getExistedUser(u.name, u.pwd) foreach f(2)
  register(u.name, u.pwd) foreach f(3)

}
