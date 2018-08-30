package org.rebeam

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import scala.scalajs.js.annotation._


@JSExportTopLevel("Main")
object Main {

  val MainView =
    ScalaComponent.builder[String]("MainView")
      .render_P(name => <.div("Hello ", name))
      .build

  @JSExport
  def main(): Unit = {
    // Render the top-level view to the predefined HTML div with id "App"
    // MainView("World").renderIntoDOM(dom.document.getElementById("App"))
    DownshiftDemo.ctor(DownshiftDemo.Props(DownshiftDemo.countries)).renderIntoDOM(dom.document.getElementById("App"))

    ()
  }
}
