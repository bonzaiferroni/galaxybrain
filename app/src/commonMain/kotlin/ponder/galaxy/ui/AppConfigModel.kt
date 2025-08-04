package ponder.galaxy.ui

import pondui.LocalValueRepository
import pondui.ValueRepository
import pondui.ui.core.StateModel
import pondui.ui.core.ViewState

class AppConfigModel(
    private val valueRepo: ValueRepository = LocalValueRepository()
): StateModel<AppConfigState>() {
    override val state = ViewState(AppConfigState())
}

data class AppConfigState(
    val placeholder: String = ""
)