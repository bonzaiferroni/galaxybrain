package ponder.galaxy.app.ui

import pondui.LocalValueSource
import pondui.ValueRepository
import pondui.ui.core.StateModel
import pondui.ui.core.ModelState

class AppConfigModel(
    private val valueRepo: ValueRepository = LocalValueSource()
): StateModel<AppConfigState>() {
    override val state = ModelState(AppConfigState())
}

data class AppConfigState(
    val placeholder: String = ""
)