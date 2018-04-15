package sse.xs.util

import sse.xs.msg.room.{Move, Pos}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by xusong on 2018/4/2.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
object StrUtil {
  def getStepsAsString(steps: ArrayBuffer[Move]) = {
    val valueStr = steps.toList.foldLeft("")((x, y) => {
      val split = if (x.isEmpty) "" else ","
      x + split + stringOfMove(y)
    })
    "{ \"step\": [" + valueStr + "] }"
  }


  def stringOfMove(m: Move) = {
    "[" + m.from.x + "," + m.from.y + "," + m.to.x + "," + m.to.y + "]"
  }
}
object UTest extends App{
  import StrUtil._
  val a = new ArrayBuffer[Move]
  println(getStepsAsString(a))
  a.append(Move(Pos(1,1),Pos(2,2)))
  println(getStepsAsString(a))
  a.append(Move(Pos(3,3),Pos(4,5)))
  println(getStepsAsString(a))
}