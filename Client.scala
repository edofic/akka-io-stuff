import akka.actor._
import akka.io.{IO, Udp}
import akka.io.Udp.{Send, SimpleSenderReady, SimpleSender}
import akka.util.ByteString
import java.net.InetSocketAddress

class Client extends Actor with ActorBase {
  import context.system
  IO(Udp) ! SimpleSender

  def receive = known {
    case SimpleSenderReady => context.become(proxy(sender))
  }

  val Msg = "(\\d+) +(.+)".r

  def proxy(client: ActorRef) = known {
    case Msg(portText, msgText) =>
      val adr = new InetSocketAddress(portText.toInt)
      val data = ByteString(msgText)
      client ! Send(data, adr)
  }
}

object Client {
  def main(args: Array[String]){
    val sys = ActorSystem()
    val client = sys.actorOf(Props[Client])

    def step(){
      readLine("[port] message | exit:  ") match {
        case "exit" =>
          sys.shutdown()
          System exit 0
        case msg =>
          client ! msg
          step()
      }
    }
    step()
  }
}