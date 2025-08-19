package ponder.galaxy.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.galaxy.GalaxyProfileRoute
import ponder.galaxy.io.GalaxyApiClient
import ponder.galaxy.io.StarApiClient
import ponder.galaxy.model.data.Galaxy
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.Star
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel

class GalaxyProfileModel(
    private val galaxyId: GalaxyId,
    private val galaxyClient: GalaxyApiClient = GalaxyApiClient(),
    private val starClient: StarApiClient = StarApiClient(),
): StateModel<GalaxyProfileState>() {
    override val state = ModelState(GalaxyProfileState())

    init {
        viewModelScope.launch {
            val galaxy = galaxyClient.readGalaxyById(galaxyId) ?: return@launch
            val stars = starClient.readLatestByGalaxyId(galaxyId) ?: emptyList()
            setState { it.copy(galaxy = galaxy, stars = stars) }
        }
    }
}

data class GalaxyProfileState(
    val galaxy: Galaxy? = null,
    val stars: List<Star> = emptyList()
)