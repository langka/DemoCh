package main

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import learn.PingActor

import scala.concurrent.duration._

/**
  * Created by xusong on 2018/1/23.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
object Hello extends App {
  implicit val timeout = Timeout(5 seconds)
  implicit val exceutor = scala.concurrent.ExecutionContext.Implicits.global
  val system = ActorSystem(name = "nice")
  val actor = system.actorOf(Props[PingActor], name = "ping")
  readLine()
}
