package main.scala

import akka.actor.{ActorLogging, Actor}
import org.htmlparser.Parser
import org.htmlparser.filters.{HasAttributeFilter, TagNameFilter, AndFilter}
import org.htmlparser.nodes.TagNode


case class CurrentUrl(url: String)

class ParserActor(parser: Parser) extends Actor with ActorLogging {

  def receive = {

    case CurrentUrl(link) => {

      try {

        parser.setURL(link + Config.get("crawlerSuffix"));

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
