import org.scalatest.{FunSpec, FunSpecLike, Matchers}
import sse.xs.msg.game.GetGameHistory
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
      val g = GetGameHistory(1)
      dbService.requireGameHistories(g).foreach(x=>println(x.matches))


    }
  }

}
