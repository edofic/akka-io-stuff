import akka.actor._
import akka.io.Udp.Bound
import akka.io.Udp.{Received, Bind}
import akka.io.{IO, Udp}
import java.net.InetSocketAddress

class Counter(limit: Int) extends Actor with ActorBase {
  import context.system

  var time = 0l
  var last: Any = null
  var count = 0

  IO(Udp) ! Bind(self, new InetSocketAddress(0))

  def receive = known {
    case Bound(adr) =>
      val port = adr.getPort
      println(s"Bound on $port")
      context become listening
  }

  def listening: Receive = {
    case Received(msg, sender) =>
      if(msg == last){
        count += 1
        if(count > limit){
          val cur = System.currentTimeMillis()
          val took = cur - time
          val thru = count.toFloat / took
          println(s"Processed $count in $took; throughput: $thru")
          count = 0
          time = System.currentTimeMillis()
        }
      } else {
        last = msg
        time = System.currentTimeMillis()
      }
  }
}

object Counter {
  def main(args: Array[String]){
    val sys = ActorSystem()
    val limit = readLine("limit: ").toInt
    sys.actorOf(Props(new Counter(limit)))

    readLine()
    sys.shutdown()
  }
}