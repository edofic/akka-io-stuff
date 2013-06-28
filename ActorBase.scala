import akka.actor.{Actor, ActorLogging}

trait ActorBase extends ActorLogging {
  this: Actor =>

  def known(pf: Receive): Receive = pf orElse {
    case msg => log.warning("Received unknown message: {}", msg)
  }
}
