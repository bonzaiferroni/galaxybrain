package ponder.galaxy.app.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.galaxy.app.io.GalaxyApiClient
import ponder.galaxy.model.data.Galaxy
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel

class GalaxyFeedModel(
    private val galaxyClient: GalaxyApiClient = GalaxyApiClient()
): StateModel<GalaxyFeedState>() {
    override val state = ModelState(GalaxyFeedState())

    init {
        viewModelScope.launch {
            val galaxies = galaxyClient.readAll() ?: emptyList()
            setState { it.copy(galaxies = galaxies) }
        }
    }
}

data class GalaxyFeedState(
    val galaxies: List<Galaxy> = emptyList()
)