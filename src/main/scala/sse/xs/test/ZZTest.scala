package sse.xs.test

/**
  * Created by xusong on 2018/4/27.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
object ZZTest extends App {
  val r1 = "a".r
  val r2 = "ab".r
  val r3 = "aa".r

  val regexList = List(r1, r2, r3).zipWithIndex
  val list = List("+", "++", "+=")
  implicit val ordering = new Ordering[Tuple2[Int,Int]] {
    override def compare(x: (Int, Int), y: (Int, Int)) = y._1-x._1
  }
  println(regexList.map { x =>
    (x._1.findPrefixMatchOf("aa").map(_.end).getOrElse(-1),x._2)
  }.max)

}
