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
    val crawler = system.actorOf(Props[CrawlerActor], name = "crawler")
    crawler ! StartUp(siteMapLinks, 3)
    // shut down the actor system once the array is empty

  }
}

case class StartUp(links: Set[String], maxConcurrentCalls: Int)
case class ParsedLinks(links: Set[String])
case class Failure(message: Error)
case class ShutDown()  // shut down the system once the array is empty

class CrawlerActor extends Actor with ActorLogging {

  val http = new Http;
  var visitedLinks: Set[String] = Set()
  var notVisitedLinks: Set[String] = Set()
  var currentNumberOfCalls = 0


  def receive = {

    case StartUp(links, maxConcurrentCalls) => {

      val parser = context.system.actorOf(Props[ParserActor], name = "parser")

      notVisitedLinks = links
      currentNumberOfCalls = maxConcurrentCalls

      for (a <- 1 to currentNumberOfCalls) {

        if (!notVisitedLinks.isEmpty) {

          val head = notVisitedLinks.head
          parser ! CurrentUrl(head)

          notVisitedLinks = notVisitedLinks - head
          visitedLinks = visitedLinks + head
        }
      }

    }

    case ParsedLinks(links) => {
      //TODO - update the references, trigger another call if the references are not empty
      println(links)
      // or "shutDown"
      context.self ! ShutDown()
    }
    // or simply "shutDown"
    case ShutDown() => {
      println("Shutting Down")
  //    context.system.shutdown
    }

    case Failure(error) =>  println(error.getMessage())

    case _ => println("Unhandled case")
  }

}
