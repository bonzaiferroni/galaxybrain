package ponder.galaxy.ui

import ponder.galaxy.model.data.Star
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel

class StarContentModel(val star: Star): StateModel<StarContentState>() {
    override val state = ModelState(StarContentState())

    init {

    }
}

data class StarContentState(
    val content: String = ""
)