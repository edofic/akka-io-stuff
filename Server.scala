import akka.actor._
import akka.io.Udp.Bound
import akka.io.Udp.{Send, Received, Bind}
import akka.io.{IO, Udp}
import java.net.InetSocketAddress


class Server(verbose: Boolean) extends Actor with ActorBase {
  import context.system

  IO(Udp) ! Bind(self, new InetSocketAddress(0))

  def receive = known {
    case Bound(adr) =>
      val port = adr.getPort
      println(s"Bound on $port")
      context become (if(verbose) listening else echo(sender))
  }

  def listening = {
    println("chatty mode")
    known {
      case Received(data, sender) =>
        println(s"""got "${data.utf8String}" from $sender""")
    }
  }

  def echo(io: ActorRef) = {
    println("echo mode")
    known {
      case Received(data, sender) => io ! Send(data, sender)
    }
  }
}


object Server {
  def main(args: Array[String]){
    val sys = ActorSystem()
    val verbose = readLine("Verbose? [true/false] ").toBoolean
    val server = sys.actorOf(Props(new Server(verbose)))

    readLine()
    sys.shutdown()
  }
}