package sample

import scalikejdbc.config._
import akka.actor.{Props, ActorSystem}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import org.apache.commons.daemon.{Daemon, DaemonContext}

class BotDaemon extends Daemon {

  private var system: Option[ActorSystem] = None

  override def init(context: DaemonContext): Unit = {
    DBs.setup()
  }

  override def start(): Unit = {
    system = Some(ActorSystem("transiru_bot"))
    system.foreach(s => s.scheduler.schedule(0 seconds, 300 seconds, s.actorOf(Props[CheckEventActor]), ()))
  }

  override def stop(): Unit = {
    system.foreach(_.terminate)
    DBs.close()
  }

  override def destroy(): Unit = {

  }
}
