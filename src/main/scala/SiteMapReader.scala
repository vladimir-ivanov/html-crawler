package main.scala

import scala.xml.{XML, Elem}

object SiteMapReader {

  def getLinks(input: String): Set[String] = {

    val xmlElement: Elem = XML.loadString(input);

    (xmlElement \\ "loc").toList.map(x => x.text).toSet
  }

}