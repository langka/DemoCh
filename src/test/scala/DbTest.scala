import org.scalatest.{FunSpec, FunSpecLike, Matchers}
import sse.xs.msg.user.User
import sse.xs.service.DbService

/**
  * Created by xusong on 2018/3/18.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
class DbTest extends FunSpecLike with Matchers {




  describe("insert into user") {
    it("insert xs") {
    }
    it("insert game"){
      val k=DbService.saveGame(0,0,0,0,"")
      DbService.saveGame(1,7,1,7,"")
      DbService.saveGame(1,7,7,1,"")
      DbService.saveGame(1,7,1,7,"")
      println(DbService.winAndLoseOf(1))
      println(DbService.winAndLoseOf(7))
      println(k)
    }
  }

}
