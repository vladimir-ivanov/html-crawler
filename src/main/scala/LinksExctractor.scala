package main.scala

import scala.xml.{XML, Elem}

object LinksExtractor {

  def getLinks(input: String): Set[String] = {

    val xmlElement: Elem = XML.loadString(input);

    (xmlElement \\ "a").toList.map(x => x.attribute("href").toString).toSet
  }
}
