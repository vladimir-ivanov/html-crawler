package main.scala

import akka.actor.{ReceiveTimeout, ActorLogging, Actor}
import org.htmlparser.Parser
import org.htmlparser.filters.{HasAttributeFilter, TagNameFilter, AndFilter}
import org.htmlparser.nodes.TagNode
import scala.concurrent.duration.Duration

case class CurrentUrl(url: String)

//TODO - try catch everything and push via Failed case classes if not done automatically
class ParserActor extends Actor with ActorLogging {
  def receive = {

    case CurrentUrl(link) => {

      try {
        val parser: Parser = new Parser(link + Config.get("crawlerSuffix"));
        var internalHrefs: Set[String] = Set()

        log.info(link + Config.get("crawlerSuffix"))

        val linkNodes = parser.parse(new AndFilter(
          new TagNameFilter("A"),
          new HasAttributeFilter("href")
        ))

        linkNodes.toNodeArray.foreach(p => {

          var href = p.asInstanceOf[TagNode].getAttribute("href")

          if (!href.startsWith("http") && !href.startsWith("mailto") && !href.isEmpty) {

            href = Config.get("baseUrl").concat(href)
            internalHrefs = internalHrefs + href
          }
        })

        context.sender ! ParsedLinks(internalHrefs)
      } catch {
        case e: Exception => {
          val wrapperException = new Exception("Unable to parse/fetch " + link, e)
          context.sender ! Failure(wrapperException)
        }
      }
    }

//    case ReceiveTimeout => {
//      // To turn it off
//      context.setReceiveTimeout(Duration.Undefined)
//      throw new RuntimeException("Receive timed out")
//    }

  }

  //@see http://doc.akka.io/docs/akka/snapshot/scala/actors.html
}
