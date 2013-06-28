import akka.actor._
import akka.io.Udp._
import akka.io.Udp.Bind
import akka.io.Udp.Bound
import akka.io.Udp.Received
import akka.io.{IO, Udp}
import akka.util.ByteString
import java.net.InetSocketAddress

case class SendMessages(adr: InetSocketAddress, n: Int, content: ByteString, fresh: Boolean = true) extends Event

class Benchmark extends Actor with ActorBase {
  import context.system
  IO(Udp) ! Bind(self, new InetSocketAddress(0))

  def receive = known {
    case Bound(adr) => context become listener(sender)
  }

  def listener(io: ActorRef) = {
    var received = 0
    var startTime = 0l
    var expected: ByteString = null

    known {
      case Received(data, _) =>
        if(data == expected){
          received += 1
          if(received >= 10000){
            val time = System.currentTimeMillis()
            val took = time - startTime
            val per = took.toFloat / received
            println(s"\ntook $took for $received messages. $per per one. throughput ${1/per}")
            startTime = time
            received = 0
          }
        }
      case cmd @ SendMessages(_, n, content, true) if n > 0 =>
        expected = content
        received = 0
        startTime = System.currentTimeMillis()
        self ! cmd.copy(fresh = false)
      case cmd @ SendMessages(adr, n, content, false) if n > 0 =>
        val ack = cmd.copy(n = n-1)
        io ! Send(content, adr, ack)
      case SendMessages(_,0,_,_) => println("sending done")
    }
  }
}

object Benchmark {
  def main(args: Array[String]){
    val sys = ActorSystem()
    val client = sys.actorOf(Props[Benchmark])

    val Cmd = "(\\d+) +(\\d+) +(.+)".r

    def step(){
      readLine("port n msg | exit:  ") match {
        case "exit" =>
          sys.shutdown()
          System exit 0
        case Cmd(port, n, msg) =>
          val adr = new InetSocketAddress("localhost", port.toInt)
          client ! SendMessages(adr, n.toInt*10000, ByteString(msg))
        case _ => "unknown command"
      }
      step()
    }
    step()
  }
}