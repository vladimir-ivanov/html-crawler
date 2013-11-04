
import main.scala.{CurrentUrl, ParserActor}
import org.htmlparser.filters.{HasAttributeFilter, TagNameFilter, AndFilter}
import org.htmlparser.util.NodeList
import org.mockito.invocation.InvocationOnMock
import org.mockito.Mockito
import org.mockito.stubbing.Answer
import org.scalatest.mock.MockitoSugar
import scala.util.Random

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
  import TestKitUsageSpec._

//  val echoRef = system.actorOf(Props[EchoActor])
//  val forwardRef = system.actorOf(Props(classOf[ForwardingActor], testActor))
//  val filterRef = system.actorOf(Props(classOf[FilteringActor], testActor))
//  val randomHead = Random.nextInt(6)
//  val randomTail = Random.nextInt(10)
//  val headList = immutable.Seq().padTo(randomHead, "0")
//  val tailList = immutable.Seq().padTo(randomTail, "1")
//  val seqRef =
//    system.actorOf(Props(classOf[SequencingActor], testActor, headList, tailList))

  val parser = mock[Parser]
  Mockito.when(parser.parse(new AndFilter(
    new TagNameFilter("A"),
    new HasAttributeFilter("href")
  ))).thenReturn(new NodeList())

//  Mockito.stub(parser.setURL("http://test.com")).toReturn()
//
 // Mockito.doThrow(Exception).when(mock).setURL("http://test.com");
  val parserActorRef = system.actorOf(Props(classOf[ParserActor], parser))

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

  /**
   * An Actor that echoes everything you send to it
   */
  class EchoActor extends Actor {
    def receive = {
      case msg ⇒ sender ! msg
    }
  }

  /**
   * An Actor that forwards every message to a next Actor
   */
  class ForwardingActor(next: ActorRef) extends Actor {
    def receive = {
      case msg ⇒ next ! msg
    }
  }

  /**
   * An Actor that only forwards certain messages to a next Actor
   */
  class FilteringActor(next: ActorRef) extends Actor {
    def receive = {
      case msg: String ⇒ next ! msg
      case _           ⇒ None
    }
  }

  /**
   * An actor that sends a sequence of messages with a random head list, an
   * interesting value and a random tail list. The idea is that you would
   * like to test that the interesting value is received and that you cant
   * be bothered with the rest
   */
  class SequencingActor(next: ActorRef, head: immutable.Seq[String],
                        tail: immutable.Seq[String]) extends Actor {
    def receive = {
      case msg ⇒ {
        head foreach { next ! _ }
        next ! msg
        tail foreach { next ! _ }
      }
    }
  }
}