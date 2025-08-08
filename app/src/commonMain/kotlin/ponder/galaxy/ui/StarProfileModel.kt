package ponder.galaxy.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.galaxy.globalProbeService
import ponder.galaxy.io.GalaxySource
import ponder.galaxy.io.ProbeService
import ponder.galaxy.model.data.Galaxy
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarLog
import pondui.LocalValueSource
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel

class StarProfileModel(
    starId: StarId,
    private val probeService: ProbeService = globalProbeService,
    private val galaxySource: GalaxySource = GalaxySource(),
    private val valueSource: LocalValueSource = LocalValueSource(),
): StateModel<StarProfileState>() {

    override val state = ModelState(StarProfileState())

    init {
        viewModelScope.launch {
            val star = probeService.getStar(starId) ?: return@launch
            val starLogs = probeService.readStarLogs(starId) ?: emptyList()
            val riseFactor = valueSource.readInt(RISE_FACTOR_KEY, 1)
            val galaxy = galaxySource.readGalaxyById(star.galaxyId)
            setState { it.copy(star = star, starLogs = starLogs, riseFactor = riseFactor, galaxy = galaxy) }

            probeService.stateFlow.collect { state ->
                val starLogs = state.starLogs[starId] ?: return@collect
                setState { it.copy(starLogs = starLogs)}
            }
        }
    }
}

data class StarProfileState(
    val star: Star? = null,
    val galaxy: Galaxy? = null,
    val starLogs: List<StarLog> = emptyList(),
    val riseFactor: Int = 1,
)