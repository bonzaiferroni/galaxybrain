package ponder.galaxy.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.galaxy.globalProbeService
import ponder.galaxy.io.ProbeService
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel

class StarProfileModel(
    starId: StarId,
    private val probeService: ProbeService = globalProbeService
): StateModel<StarProfileState>() {

    override val state = ModelState(StarProfileState())

    init {
        viewModelScope.launch {
            val star = probeService.getStar(starId)
            setState { it.copy(star = star) }
        }
    }
}

data class StarProfileState(
    val star: Star? = null
)