package ponder.galaxy.io

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ponder.galaxy.model.Api
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarLog
import pondui.io.ApiClient
import pondui.io.globalApiClient
import pondui.ui.core.ModelState
import kotlin.collections.containsKey
import kotlin.collections.get
import kotlin.collections.putAll
import kotlin.text.get
import kotlin.text.set

class ProbeService(
    private val probeSocket: ProbeSocket = ProbeSocket(),
    private val starSource: StarSource = StarSource(),
    private val apiClient: ApiClient = globalApiClient,
) {
    private val state = ModelState(ProbeServiceState())
    val stateFlow: StateFlow<ProbeServiceState> = state

    private val allStarLogs = mutableMapOf<StarId, List<StarLog>>()
    private val allStars = mutableMapOf<StarId, Star>()

    private var isStarted = false

    fun start() {
        println("starting probe service")
        if (isStarted) return
        isStarted = true
        probeSocket.start()

        CoroutineScope(Dispatchers.IO).launch {
            probeSocket.probeFlow.collect { galaxyProbe ->
                val galaxyId = galaxyProbe.galaxyId; val starLogs = galaxyProbe.starLogs
                val missingStarIds = starLogs.filter { starLog -> !allStars.containsKey(starLog.starId) }.map {it.starId}
                starSource.readStars(missingStarIds).forEach { allStars[it.starId] = it }

                val missingStarLogs = starSource.readStarLogs(missingStarIds)
                allStarLogs.putAll(missingStarLogs)

                for (starLog in starLogs) {
                    if (missingStarIds.any { starLog.starId == it }) continue
                    val currentList = allStarLogs[starLog.starId] ?: continue
                    allStarLogs[starLog.starId] = currentList + starLog
                }

                state.setValue { it.copy(stars = allStars.values.toList()) }
            }
        }
    }

    fun getRise(starId: StarId) = allStarLogs[starId]?.lastOrNull()?.rise

    fun getStarLogs(starId: StarId) = allStarLogs[starId]

    suspend fun getStar(starId: StarId) = allStars[starId] ?: apiClient.get(Api.Stars, starId).also { allStars[starId] = it }
}

data class ProbeServiceState(
    val stars: List<Star> = emptyList(),
)