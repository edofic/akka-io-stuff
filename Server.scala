import akka.actor._
import akka.io.{IO, Udp}
import java.net.InetSocketAddress


class Server extends Actor with ActorBase {
  import context.system

  IO(Udp) ! Udp.Bind(self, new InetSocketAddress(0))

  def receive = known {
    case Udp.Bound(adr) =>
      val port = adr.getPort
      println(s"Bound on $port")
      context become listening
  }

  def listening = known {
    case Udp.Received(data, sender) =>
      println(s"""got "${data.utf8String}" from $sender""")
  }

}


object Server {
  def main(args: Array[String]){
    val sys = ActorSystem()
    val server = sys.actorOf(Props[Server])

    readLine()
    sys.shutdown()
  }
}