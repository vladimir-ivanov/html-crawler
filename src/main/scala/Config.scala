package main.scala

import java.util.Properties

object Config {

  def get(key: String): String = {
    try {
      val prop = new Properties

      prop.load(getClass.getClassLoader.getResourceAsStream("config.properties"));

      prop.getProperty(key)


    } catch {
      case e: Exception =>
        e.printStackTrace()
        sys.exit(1)
    }

  }
}
