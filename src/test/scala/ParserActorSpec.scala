
import main.scala.{CurrentUrl, ParserActor, Config}
import org.htmlparser.filters.{HasAttributeFilter, TagNameFilter, AndFilter}
import org.htmlparser.util.NodeList
import org.mockito.invocation.InvocationOnMock
import org.mockito.Mockito
import org.mockito.stubbing.Answer
import org.scalatest.mock.MockitoSugar
import scala.util.Random

import org.mockito.Matchers.any

import org.htmlparser.Parser

import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpecLike
import org.scalatest.Matchers

import com.typesafe.config.ConfigFactory

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.DefaultTimeout
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import scala.concurrent.duration._
import scala.collection.immutable
import org.mockito

/**
 * a Test to show some TestKit examples
 */
class TestKitUsageSpec
  extends TestKit(ActorSystem("CrawlerSystem",
    ConfigFactory.parseString(TestKitUsageSpec.config)))
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll with MockitoSugar {

  val parser = mock[Parser]

  Mockito.stub(parser.parse(any(classOf[AndFilter]))).toReturn(new NodeList)

  val parserActorRef = system.actorOf(Props(classOf[ParserActor], parser, Config.get("crawlerSuffix")))

  override def afterAll {
    shutdown(system)
  }

  "An EchoActor" should {
    "Respond with the same message it receives" in {
      within(500 millis) {
        parserActorRef ! CurrentUrl("http://test.com")
        expectMsg("test")
      }
    }
  }

}

object TestKitUsageSpec {
  // Define your test specific configuration here
  val config = """
    akka {
      loglevel = "WARNING"
    }
               """
}