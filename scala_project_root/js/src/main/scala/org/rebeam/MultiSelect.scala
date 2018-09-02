package org.rebeam

import scala.scalajs.js

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._

import org.rebeam.downshift.Downshift
import org.rebeam.downshift.Downshift._

object MultiSelect {

  case class Props[A](
    items: List[A], 
    selectedItems: List[A], 
    onSelectionChange: List[A] => Callback,
    itemToString: A => String = (a: A) => a.toString, 
    itemToKey: (A, Int) => String = (_: A, i: Int) => i.toString,
    maxMenuItems: Int = 5,
    nothingFound: String = "Nothing found...",
    moreAvailable: String = "More items match..."
  )

  case class State(inputValue: String)

  class Backend[A](scope: BackendScope[Props[A], State]) {

    def renderItem(itemAsString: String, index: Int, itemProps: js.Object, highlightedIndex: Option[Int]): VdomElement = {
      mui.MenuItem(
        key = itemAsString,
        selected = highlightedIndex.contains(index),
        component = "div": js.Any,
        // Downshift has provided us with an object containing props to make autocompletion work.
        // We can pass these to additionalProps, and they will be applied as props of the underlying
        // material-ui MenuItem
        additionalProps = itemProps
      )(itemAsString)
    }

    // TODO can we use the state reducer here? We could then reselect the same
    // highlight index to avoid glitch until mouse is moved?
    private val handleChange = (item: Option[A], c: RenderState[A]) => for {
      p <- scope.props

      // If we have a new item, fire event with new selection
      _ <- item
            .filter(i => !p.selectedItems.contains(i))
            .fold(
              Callback.empty
            )(
              newItem => p.onSelectionChange(p.selectedItems :+ newItem)
            )
      
      // Reopen the menu and select the first entr
      _ <- Callback {
        c.openMenu()
        c.setHighlightedIndex(0)
      }
    } yield ()

    // On input value change, update inputValue in state
    private val handleInputValueChange = (value: String, c: RenderState[A]) => scope.modState(_.copy(inputValue = value))

    // On clicking delete in a chip, fire event deleting the corresponding item from selection
    private def handleDelete(item: A): Callback = for {
      p <- scope.props
      _ <- if (p.selectedItems.contains(item)) {
        p.onSelectionChange(p.selectedItems.filter(_ != item))
      } else {
        Callback.empty
      } 
    } yield ()

    //Provided directly to JS props, so needs to be a js function that runs immediately
    private val handleKeyDown: js.Function1[ReactKeyboardEvent, Unit] = (e: ReactKeyboardEvent) => {
      for {
        p <- scope.props
        s <- scope.state
        _ <- if (e.key.toLowerCase == "backspace" && s.inputValue.isEmpty) {
          p.onSelectionChange(p.selectedItems.dropRight(1))
        } else {
          Callback.empty
        }
      } yield ()
    }.runNow

    def render(props: Props[A], state: State) = {

      <.div(
        ^.flexGrow := "1",
        ^.position := "relative",

        Downshift[A](
          itemToString = (a: A) => props.itemToString(a),
          onChange = handleChange,
          inputValue = state.inputValue,
          onInputValueChange = handleInputValueChange,
          selectedItem = None            
        )(

          (a: RenderState[A]) => {

            //Chips to display current selected items
            //Provided to a js object, so needs to be a raw node
            val chips = props.selectedItems.zipWithIndex.toVdomArray{
              case(item, index) => {
                val itemAsString = props.itemToString(item)
                mui.Chip(
                  key = props.itemToKey(item, index),
                  tabIndex = -1: js.Any,
                  label = props.itemToString(item): VdomNode,
                  onDelete = handleDelete(item),
                  style = mui.styles.Style(
                    "margin" -> "3px 6px 3px 0px"
                  )
                )
              }
            }.rawNode.asInstanceOf[js.Any]  //TODO why do we need to cast this? Otherwise leads to diverging implicit expansion for type scala.scalajs.js.|.Evidence[A1,Short] when trying to set in literal below

            val openAndHighlightFirst: js.Function0[Unit] = () => {
              a.openMenu()
              a.setHighlightedIndex(0)
            }

            val allSuggestions = props.items.filter(
              i => !props.selectedItems.contains(i) && 
                    props.itemToString(i).toLowerCase.contains(a.inputValue.toLowerCase)
            )

            val (suggestions, overflow) = if (allSuggestions.size > props.maxMenuItems) {
              (allSuggestions.take(props.maxMenuItems), true)
            } else {
              (allSuggestions, false)
            }

            val menuItems = suggestions.zipWithIndex.map {
              case (item, index) => 
                renderItem(
                  props.itemToString(item), 
                  index,
                  a.getItemProps(ItemData(item, index, disabled = false)),
                  a.highlightedIndex
                )
            }

            val menuContents = if (menuItems.isEmpty){
              List[VdomElement](
                mui.MenuItem(
                  key = "org.rebeam.MultiSelect.ellipsis",
                  selected = false,
                  component = "div": js.Any,
                  disabled = true,
                  button = false
                )(props.nothingFound)
              )
            } else if (overflow) {
              menuItems :+ (mui.MenuItem(
                key = "org.rebeam.MultiSelect.ellipsis",
                selected = false,
                component = "div": js.Any,
                disabled = true,
                button = false
              )(props.moreAvailable): VdomElement)
            } else {
              menuItems
            }

            // Get properties for input from Downshift, providing
            // our desired properties as a JS object
            val inputProps = 
              a.getInputProps(
                js.Dynamic.literal(
                  "placeholder" -> "Search for a country",
                  "startAdornment" -> chips,

                  // We need to pass this in here or downshift will override it
                  "onKeyDown" -> handleKeyDown,

                  "onFocus" -> openAndHighlightFirst,

                  // Input should wrap, in case we have a lot of chips
                  "style" -> js.Dynamic.literal(
                    "flexWrap" -> "wrap"
                  ),

                  // This is passed through to the actual <input> used by the
                  // textfield, we set the style so that the input is the same
                  // size as the chips (so that the component doesn't change size
                  // when adding the first chip), and has a better text alignment 
                  // to chips.
                  "inputProps" -> js.Dynamic.literal(
                    "style" -> js.Dynamic.literal(
                      "paddingTop" -> "10px",
                      "paddingBottom" -> "12px",
                    ),
                  ),
                )
              )

            <.div(

              mui.TextField(
                fullWidth = true, 
                InputProps = inputProps
              ),

              mui.Paper(
                square = true,
                style = mui.styles.Style(
                  "position" -> "absolute",
                  "zIndex" -> "1",
                  "marginTop" -> "4px",
                  "left" -> "0",
                  "right" -> "0"
                )
              )(
                menuContents: _*
              ).when(a.isOpen)

            )
          }
        )
      )
    }
  }

  //Just make the component constructor - props to be supplied later to make a component
  def component[A] = ScalaComponent.builder[Props[A]]("DownshiftDemo")
    .initialState(State(""))
    .backend(new Backend[A](_))
    .render(s => s.backend.render(s.props, s.state))
    .build

  // /**
  //   * Create a MultiSelect
  //   * @param items  The items available for selection (can include already 
  //   *               selected items, these will not be displayed for reselection)
  //   * @param selectedItems  The currently selected items. Do not need to be in items,
  //   *                       if you want to remove selectedItems that are not in items
  //   *                       you need to do this yourself.
  //   * @param onSelectionChange  Will be called when selection changes, with the new
  //   *                           selection list. Generally you will update state in
  //   *                           a parent component, causing a render with new selectedItems.
  //   * @param itemToString Function to display an item as a string, as edited in the input
  //   *                     and displayed in chips. Defaults to toString.
  //   * @param itemToKey  Function to produce a key for an item in a list of items - also
  //   *                   provided with the index in the list - this defaults to just using 
  //   *                   the index.
  //   * @param maxMenuItems Maximum number of items to display in the menu. If additional
  //   *                     matching items exist an ellipsis will be displayed to indicate this.
  //   * @return A MultiSelect component
  //   */
  // def apply[A](
  //   items: List[A], 
  //   selectedItems: List[A], 
  //   onSelectionChange: List[A] => Callback,
  //   itemToString: A => String = (a: A) => a.toString, 
  //   itemToKey: (A, Int) => String = (_: A, i: Int) => i.toString,
  //   maxMenuItems: Int = 5
  // ) = component[A](Props(items, selectedItems, onSelectionChange, itemToString, itemToKey, maxMenuItems))

}
