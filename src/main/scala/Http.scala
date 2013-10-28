package main.scala

import java.net.{HttpURLConnection, URL}
import java.io.{InputStreamReader, BufferedReader}


class Http {
  def get(url: String): String = {

    val urlObject: URL = new URL(url)
    val con: HttpURLConnection = urlObject.openConnection.asInstanceOf[HttpURLConnection];

    con.setRequestMethod("GET")


    //val responseCode: Int = con.getResponseCode

    //    System.out.println("\nSending 'GET' request to URL : " + url)
    //    System.out.println("Response Code : " + responseCode)

    val in: BufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream))

    var inputLine: String = null
    val response: StringBuffer = new StringBuffer

    while ( {
      inputLine = in.readLine;
      inputLine != null
    }) {
      response.append(inputLine)
    }

    in.close()

    response.toString
  }
}
