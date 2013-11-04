package main.scala

import akka.actor._
import scala.Predef._
import org.htmlparser.Parser


object Crawler {

  def main(args: Array[String]) = {


    val siteMapUrl = Config.get("siteMapUrl")
    val http = new Http;

    val rawResponse = http.get(siteMapUrl);

    val siteMapLinks = SiteMapReader.getLinks(rawResponse);

    val system = ActorSystem("CrawlerSystem")
    //  val log = akka.event.Logging.getLogger(system)

    val parser = system.actorOf(Props(classOf[ParserActor], new Parser), name = "parser")
    val crawler = system.actorOf(Props(classOf[CrawlerActor], new Http, parser), name = "crawler")

    crawler ! StartUp(siteMapLinks, 5)
  }
}

case class StartUp(links: Set[String], maxConcurrentCalls: Int)

case class ParsedLinks(links: Set[String])

case class Failure(message: Exception)

case class ShutDown()


class CrawlerActor(http: Http, parser: ActorRef) extends Actor with ActorLogging {

  var visitedLinks: Set[String] = Set()
  var notVisitedLinks: Set[String] = Set()
  var currentNumberOfCallsAtStart = 0


  def receive = {

    case StartUp(links, maxConcurrentCalls) => {

      notVisitedLinks = links

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
