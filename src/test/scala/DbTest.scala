import org.scalatest.{FunSpec, FunSpecLike, Matchers}
import sse.xs.msg.user.User
import sse.xs.service.DbService

/**
  * Created by xusong on 2018/3/18.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
class DbTest extends FunSpecLike with Matchers {


  val dbService = new DbService

  describe("insert into user") {
    it("insert xs") {
    }
    it("insert game"){
      val k=dbService.saveGame(0,0,0,0,"")
      dbService.saveGame(1,7,1,7,"")
      dbService.saveGame(1,7,7,1,"")
      dbService.saveGame(1,7,1,7,"")
      println(dbService.winAndLoseOf(1))
      println(dbService.winAndLoseOf(7))
      println(k)
    }
  }

}
