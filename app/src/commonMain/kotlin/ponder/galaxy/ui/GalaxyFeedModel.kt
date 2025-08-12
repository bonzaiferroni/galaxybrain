package ponder.galaxy.ui

import androidx.lifecycle.viewModelScope
import kabinet.model.SpeechRequest
import kabinet.model.SpeechVoice
import kabinet.utils.toAgoDescription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.galaxy.globalProbeService
import ponder.galaxy.io.GalaxySource
import ponder.galaxy.io.ProbeService
import ponder.galaxy.model.Api
import ponder.galaxy.model.data.Galaxy
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId
import pondui.LocalValueSource
import pondui.io.GeminiApiClient
import pondui.ui.core.StateModel
import pondui.ui.core.ModelState
import kotlin.math.min
import kotlin.time.Duration.Companion.minutes

class GalaxyFeedModel(
    private val probeService: ProbeService = globalProbeService,
    private val galaxySource: GalaxySource = GalaxySource(),
    private val valueSource: LocalValueSource = LocalValueSource(),
): StateModel<GalaxyFlowState>() {
    override val state = ModelState(GalaxyFlowState())

    // val messenger = MessengerModel()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val galaxies = galaxySource.readAll()
            val activeGalaxyNames = valueSource.readObjectOrNull<List<String>>(ACTIVE_GALAXY_NAMES_KEY) ?: galaxies.map { it.name }
            val filteredNames = activeGalaxyNames.filter { galaxyName -> galaxies.any { it.name == galaxyName} }
            val riseFactor = valueSource.readInt(RISE_FACTOR_KEY, 1)
            withContext(Dispatchers.Main) {
                setState { it.copy(galaxies = galaxies, activeGalaxyNames = filteredNames, riseFactor = riseFactor) }
            }

            probeService.stateFlow.collect { state ->
                withContext(Dispatchers.Main) {
                    setStars(state.stars)
                }
            }
        }
    }

    private fun setStars(stars: List<Star>) {
        val filteredStars = stars.filter { star ->
            val galaxy = stateNow.galaxies.firstOrNull { it.galaxyId == star.galaxyId } ?: return@filter false
            stateNow.activeGalaxyNames.contains(galaxy.name)
        }
            .take(100)

        setState { it.copy(stars = filteredStars) }
    }

    fun toggleGalaxy(galaxyName: String) {
        val activeGalaxyNames = if (stateNow.activeGalaxyNames.contains(galaxyName)) stateNow.activeGalaxyNames - galaxyName
        else stateNow.activeGalaxyNames + galaxyName
        valueSource.writeObject(ACTIVE_GALAXY_NAMES_KEY, activeGalaxyNames)
        setState { it.copy(activeGalaxyNames = activeGalaxyNames) }
        setStars(probeService.getStars())
    }

    fun getStarLogs(starId: StarId) = probeService.getStarLogs(starId)

    fun toggleGalaxyCloud(isVisible: Boolean = !stateNow.isGalaxyCloudVisible) {
        setState { it.copy(isGalaxyCloudVisible = isVisible) }
    }

    fun getStarsAfterIndex(index: Int, count: Int = 4) = when {
        index < stateNow.stars.size -> stateNow.stars.subList(index, min(index + count, stateNow.stars.size))
        else -> emptyList()
    }

    fun setNormalized(isNormalized: Boolean) {
        setState { it.copy(isNormalized = isNormalized) }
    }

    fun setRiseFactor(value: Int) {
        valueSource.writeInt(RISE_FACTOR_KEY, value)
        setState { it.copy(riseFactor = value) }
        setStars(stateNow.stars)
    }
}

data class GalaxyFlowState(
    val stars: List<Star> = emptyList(),
    val galaxies: List<Galaxy> = emptyList(),
    val activeGalaxyNames: List<String> = emptyList(),
    val isGalaxyCloudVisible: Boolean = false,
    val isNormalized: Boolean = true,
    val riseFactor: Int = 1,
)

const val ACTIVE_GALAXY_NAMES_KEY = "active_galaxies"
const val RISE_FACTOR_KEY = "rise_factor"