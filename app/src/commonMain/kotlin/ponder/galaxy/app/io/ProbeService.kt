package ponder.galaxy.app.io

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ponder.galaxy.app.ui.RISE_FACTOR_KEY
import ponder.galaxy.model.Api
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarLog
import pondui.LocalValueSource
import pondui.io.NeoApiClient
import pondui.io.globalNeoApiClient
import pondui.ui.core.ModelState

class ProbeService(
    private val probeSocket: ProbeSocket = ProbeSocket(),
    private val starApiClient: StarApiClient = StarApiClient(),
    private val apiClient: NeoApiClient = globalNeoApiClient,
    private val valueSource: LocalValueSource = LocalValueSource(),
) {
    private val state = ModelState(ProbeServiceState())
    val stateFlow: StateFlow<ProbeServiceState> = state

    private val allStarLogs = mutableMapOf<StarId, List<StarLog>>()
    private val allStars = mutableMapOf<StarId, Star>()

    private var isStarted = false

    init {
        val rise = valueSource.readInt(RISE_FACTOR_KEY, 1)
        state.setValue { it.copy(riseFactor = rise) }
    }

    fun start() {
        println("starting probe service")
        if (isStarted) return
        isStarted = true
        probeSocket.start()

        CoroutineScope(Dispatchers.IO).launch {
            probeSocket.probeFlow.collect { galaxyProbe ->
                val galaxyId = galaxyProbe.galaxyId;
                val starLogs = galaxyProbe.starLogs
                val missingStarIds =
                    starLogs.filter { starLog -> !allStars.containsKey(starLog.starId) }.map { it.starId }
                starApiClient.readStars(missingStarIds)?.forEach { allStars[it.starId] = it }

                starApiClient.readStarLogs(missingStarIds)?.let { allStarLogs.putAll(it) }


                for (starLog in starLogs) {
                    if (missingStarIds.any { starLog.starId == it }) continue
                    val currentList = allStarLogs[starLog.starId] ?: continue
                    allStarLogs[starLog.starId] = currentList + starLog
                }

                state.setValue { state ->
                    state.copy(
                        stars = allStars.values.sortedByDescending { getRise(it.starId) },
                        starLogs = allStarLogs
                    )
                }
            }
        }
    }

    fun getStars() = allStars.values.toList()

    fun getRise(starId: StarId) =
        allStars[starId]?.let { allStarLogs[starId]?.lastOrNull()?.getRise(it.createdAt, state.value.riseFactor) }

    fun getStarLogs(starId: StarId) = allStarLogs[starId]

    suspend fun readStarLogs(starId: StarId) = allStarLogs[starId] ?: apiClient.getById(Api.StarLogs, starId)
        ?.also { allStarLogs[starId] = it }

    suspend fun getStar(starId: StarId) = allStars[starId] ?: apiClient.getById(Api.Stars, starId)
        ?.also { allStars[starId] = it }

}

data class ProbeServiceState(
    val stars: List<Star> = emptyList(),
    val starLogs: Map<StarId, List<StarLog>> = emptyMap(),
    val riseFactor: Int = 1,
)