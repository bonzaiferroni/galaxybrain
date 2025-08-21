package ponder.galaxy.ui

import androidx.lifecycle.ViewModel
import ponder.galaxy.model.data.Star
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel
import pondui.ui.core.SubModel

class StarContentModel(
    override val viewModel: ViewModel,
    val star: Star
): SubModel<StarContentState>() {
    override val state = ModelState(StarContentState())

    init {

    }
}

data class StarContentState(
    val content: String = ""
)