package org.rebeam

import scala.scalajs.js

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._

import org.rebeam.downshift.Downshift
import org.rebeam.downshift.Downshift._

object DownshiftMultiDemo {

  val countries: List[String] = List (
    "Afghanistan",
    "Aland Islands",
    "Albania",
    "Algeria",
    "American Samoa",
    "Andorra",
    "Angola",
    "Anguilla",
    "Antarctica",
    "Antigua and Barbuda",
    "Argentina",
    "Armenia",
    "Aruba",
    "Australia",
    "Austria",
    "Azerbaijan",
    "Bahamas",
    "Bahrain",
    "Bangladesh",
    "Barbados",
    "Belarus",
    "Belgium",
    "Belize",
    "Benin",
    "Bermuda",
    "Bhutan",
    "Bolivia, Plurinational State of",
    "Bonaire, Sint Eustatius and Saba",
    "Bosnia and Herzegovina",
    "Botswana",
    "Bouvet Island",
    "Brazil",
    "British Indian Ocean Territory",
    "Brunei Darussalam"
  )

  def getSuggestions(input: String, items: List[String]): List[String] = items.filter(_.toLowerCase.contains(input.toLowerCase)).take(5)

  case class Props(items: List[String])

  case class State(selectedItems: List[String], inputValue: String)

  class Backend(scope: BackendScope[Props, State]) {

    def renderItem(item: String, index: Int, itemProps: js.Object, highlightedIndex: Option[Int], selectedItem: Option[String]): VdomElement = {
      mui.MenuItem(
        key = item,
        selected = highlightedIndex.contains(index),
        component = "div": js.Any,

        // Downshift has provided us with an object containing props to make autocompletion work.
        // We can pass these to additionalProps, and they will be applied as props of the underlying
        // material-ui MenuItem
        additionalProps = itemProps,

        // Additional style for selection
        style = mui.styles.Style(
          "fontWeight" -> (if (selectedItem.contains(item)) "500" else "400")
        )
      )(item)
    }

    // On selection change, add item to selection, and clear input
    private val handleChange = (item: Option[String], c: RenderState[String]) => 
            Callback{println(s"onChange, item $item")} >> 
            scope.modState(
              s => s.copy(
                selectedItems = item.map(i => if (s.selectedItems.contains(i)) s.selectedItems else s.selectedItems :+ i).getOrElse(s.selectedItems), 
                inputValue = ""
              )
            )

    // On input value change, update inputValue in state
    private val handleInputValueChange = (value: String, c: RenderState[String]) => 
            Callback{println(s"onInputValueChange, value $value")} >> scope.modState(_.copy(inputValue = value))

    private def handleDelete(item: String): Callback = 
      scope.modState(s => s.copy(selectedItems = s.selectedItems.filter(_ != item)))

    //TODO
    // 1. mui Styles
    def render(props: Props, state: State) = {

      <.div(
        Downshift[String](
          itemToString = (i: String) => i.toString,
          onChange = handleChange,
          inputValue = state.inputValue,
          onInputValueChange = handleInputValueChange,
          selectedItem = None            
        )(

          (a: RenderState[String]) => {

            // val chips = <.span(state.selectedItems.mkString(", "): String).rawNode.asInstanceOf[js.Any]

            //Chips to display current selected items
            val chips = state.selectedItems.toVdomArray(
              item => (
                mui.Chip(
                  key = item,
                  tabIndex = -1: js.Any,
                  label = item: VdomNode,
                  // className={classes.chip}
                  onDelete = handleDelete(item)
                )
              )
            ).rawNode.asInstanceOf[js.Any]  //TODO why do we need to cast this? Otherwise leads to diverging implicit expansion for type scala.scalajs.js.|.Evidence[A1,Short] when trying to set in literal below

            // Get properties for input from Downshift, providing
            // our desired properties as a JS object
            val inputProps = 
              a.getInputProps(
                js.Dynamic.literal(
                  "placeholder" -> "Search for a country",
                  "startAdornment" -> chips
                )
              )

            // js.Dynamic.global.console.log(inputProps)

            <.div(

              mui.TextField(
                fullWidth = true, 
                InputProps = inputProps
              ),

              mui.Paper(square = true)(
                getSuggestions(a.inputValue, props.items).zipWithIndex.map{
                  case (item, index) => renderItem(
                    item, 
                    index,
                    a.getItemProps(ItemData(item, index, disabled = false)),
                    a.highlightedIndex,
                    a.selectedItem
                  )
                }: _*
              ).when(a.isOpen)

            )

          }
        ),

        <.span(state.selectedItems.mkString(", "): String)
      )

    }

  }

  //Just make the component constructor - props to be supplied later to make a component
  def ctor = ScalaComponent.builder[Props]("DownshiftDemo")
    .initialState(State(Nil, ""))
    .backend(new Backend(_))
    .render(s => s.backend.render(s.props, s.state))
    .build

}
