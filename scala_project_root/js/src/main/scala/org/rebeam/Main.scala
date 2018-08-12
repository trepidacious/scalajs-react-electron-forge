package org.rebeam

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom

object Main {

  val MainView =
    ScalaComponent.builder[String]("MainView")
      .render_P(name => <.div("Hello ", name))
      .build

  def main(args: Array[String]): Unit = {
    // Render the top-level view to the predefined HTML div with id "App"
    MainView("World").renderIntoDOM(dom.document.getElementById("App"))
  }
}
