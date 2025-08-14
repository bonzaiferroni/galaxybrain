package ponder.galaxy.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ponder.galaxy.globalProbeService
import ponder.galaxy.io.GalaxySource
import ponder.galaxy.io.IdeaApiClient
import ponder.galaxy.io.ProbeService
import ponder.galaxy.model.data.Galaxy
import ponder.galaxy.model.data.Idea
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarLog
import pondui.LocalValueSource
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel

class StarProfileModel(
    private val starId: StarId,
    private val probeService: ProbeService = globalProbeService,
    private val galaxySource: GalaxySource = GalaxySource(),
    private val valueSource: LocalValueSource = LocalValueSource(),
    private val ideaApiClient: IdeaApiClient = IdeaApiClient(),
): StateModel<StarProfileState>() {

    override val state = ModelState(StarProfileState())

    init {
        viewModelScope.launch {
            val star = probeService.getStar(starId) ?: return@launch
            val starLogs = probeService.readStarLogs(starId) ?: emptyList()
            val riseFactor = valueSource.readInt(RISE_FACTOR_KEY, 1)
            val galaxy = galaxySource.readGalaxyById(star.galaxyId)
            val idea = ideaApiClient.readContentIdea(starId, false)
            setState { it.copy(
                star = star,
                starLogs = starLogs,
                riseFactor = riseFactor,
                galaxy = galaxy,
                contentIdea = idea,
            ) }

            probeService.stateFlow.collect { state ->
                val starLogs = state.starLogs[starId] ?: return@collect
                setState { it.copy(starLogs = starLogs)}
            }
        }
    }

    fun generateAudio() {
        viewModelScope.launch(Dispatchers.IO) {
            val idea = ideaApiClient.readContentIdea(starId, true) ?: return@launch
            setState { it.copy(contentIdea = idea, isPlaying = true) }
        }
    }

    fun toggleIsPlaying(value: Boolean = !stateNow.isPlaying) {
        setState { it.copy(isPlaying = value)}
    }
}

data class StarProfileState(
    val star: Star? = null,
    val galaxy: Galaxy? = null,
    val starLogs: List<StarLog> = emptyList(),
    val riseFactor: Int = 1,
    val contentIdea: Idea? = null,
    val isPlaying: Boolean = false,
)