package main.scala

import akka.actor._
import scala.Predef._


object Crawler {

  def main(args: Array[String]) = {



    val siteMapUrl = Config.get("siteMapUrl")
    val http = new Http;

    val rawResponse = http.get(siteMapUrl);

    val siteMapLinks = SiteMapReader.getLinks(rawResponse);

    val system = ActorSystem("CrawlerSystem")
    val log = akka.event.Logging(system, this)
    val crawler = system.actorOf(Props[CrawlerActor], name = "crawler")

    crawler ! StartUp(siteMapLinks, 5)
    // shut down the actor system once the array is empty

  }
}

case class StartUp(links: Set[String], maxConcurrentCalls: Int)

case class ParsedLinks(links: Set[String])

case class Failure(message: Exception)

case class ShutDown()

// shut down the system once the array is empty

class CrawlerActor extends Actor with ActorLogging {

  val http = new Http;
  var visitedLinks: Set[String] = Set()
  var notVisitedLinks: Set[String] = Set()
  var currentNumberOfCallsAtStart = 0
  var parser: ActorRef = null

  override def postStop(): Unit = {
    log.info(context.self.toString + " - Actor Stopped")
  }


  def receive = {

    case StartUp(links, maxConcurrentCalls) => {

      parser = context.system.actorOf(Props[ParserActor], name = "parser")

      notVisitedLinks = links
      //TODO - make currentNumberOfCallsAtStart to equal length of Set if less than maxConcurrentCalls
      currentNumberOfCallsAtStart = maxConcurrentCalls

      for (a <- 1 to currentNumberOfCallsAtStart) {



        if (!notVisitedLinks.isEmpty) {

          val head = notVisitedLinks.head

          log.info("About to parse: " + head)

          parser ! CurrentUrl(head)

          notVisitedLinks = notVisitedLinks - head
          visitedLinks = visitedLinks + head
        }
      }

    }


    case ParsedLinks(links) => {
      //TODO - update the references, trigger another call if the references are not empty
      log.info("There are " + links.size + " links")

      links.foreach(p => {

        if (!visitedLinks.contains(p)) {

          notVisitedLinks = notVisitedLinks + p
        }
      })
      // or "shutDown"

      if (notVisitedLinks.isEmpty) {
        context.self ! ShutDown()

      } else {

        val head = notVisitedLinks.head
        parser ! CurrentUrl(head)

        notVisitedLinks = notVisitedLinks - head
        visitedLinks = visitedLinks + head

      }

    }
    // or simply "shutDown"
    case ShutDown() => {
      log.info("Shutting Down")
      context.system.shutdown
    }

    case Failure(error) => {
      log.error(error.getMessage())

      val head = notVisitedLinks.head
      parser ! CurrentUrl(head)

      notVisitedLinks = notVisitedLinks - head
      visitedLinks = visitedLinks + head
    }

    case _ => log.warning("Unhandled case")
  }

}
