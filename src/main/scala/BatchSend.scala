import akka.actor._
import akka.io.{IO, Udp}
import akka.io.Udp._
import akka.util.ByteString
import java.net.InetSocketAddress

class BatchSend extends Actor with ActorBase {
  import context.system
  IO(Udp) ! Bind(self, new InetSocketAddress(0))

  def receive = known {
    case Bound(adr) => context become listener(sender)
  }

  def listener(io: ActorRef): Receive = {
    case SendMessages(_,0,_,_) => println("sending done")
    case cmd @ SendMessages(adr, n, content, _) =>
      val ack = cmd.copy(n = n-1)
      io ! Send(content, adr, ack)
  }
}

object BatchSend {
  def main(args: Array[String]){
    val sys = ActorSystem()
    val client = sys.actorOf(Props[BatchSend])

    val Cmd = "(\\d+) +(\\d+) +(.+)".r

    def step(){
      readLine("port n msg | exit:  ") match {
        case "exit" =>
          sys.shutdown()
          System exit 0
        case Cmd(port, n, msg) =>
          val adr = new InetSocketAddress("localhost", port.toInt)
          client ! SendMessages(adr, n.toInt, ByteString(msg))
        case _ => "unknown command"
      }
      step()
    }
    step()
  }
}