package sse.xs.msg.conn

/**
  * Created by xusong on 2018/3/14.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
sealed trait ConnMsg

case class ConnRequest(id:Int) extends ConnMsg

case class ConnResp(id:Int,token:Long) extends ConnMsg
