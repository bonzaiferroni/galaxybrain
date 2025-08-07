package ponder.galaxy.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.galaxy.globalProbeService
import ponder.galaxy.io.ProbeService
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarLog
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
            val starLogs = probeService.readStarLogs(starId) ?: emptyList()
            setState { it.copy(star = star, starLogs = starLogs) }

            probeService.stateFlow.collect { state ->
                val starLogs = state.starLogs[starId] ?: return@collect
                setState { it.copy(starLogs = starLogs)}
            }
        }
    }
}

data class StarProfileState(
    val star: Star? = null,
    val starLogs: List<StarLog> = emptyList()
)