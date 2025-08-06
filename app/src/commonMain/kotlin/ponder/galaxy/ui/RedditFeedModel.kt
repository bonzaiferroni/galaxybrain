package ponder.galaxy.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.galaxy.io.StarSocket
import ponder.galaxy.io.StarSource
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarLog
import pondui.ui.core.StateModel
import pondui.ui.core.ViewState

class RedditFeedModel(
    private val starSocket: StarSocket = StarSocket(),
    private val starSource: StarSource = StarSource()
): StateModel<RedditFeedState>() {
    override val state = ViewState(RedditFeedState())

    // val messenger = MessengerModel()
    private val allStarLogs = mutableMapOf<StarId, List<StarLog>>()
    private val allStars = mutableMapOf<StarId, Star>()

    init {
        viewModelScope.launch {
            starSocket.galaxyProbeFlow.collect { galaxyProbe ->
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

                val stars = allStars.values.sortedByDescending { allStarLogs[it.starId]?.lastOrNull()?.rise }
                    .take(100)

                setState { it -> it.copy(stars = stars) }
            }
        }

        viewModelScope.launch {
            starSocket.start()
        }
    }

    fun getStarLogs(starId: StarId) = allStarLogs[starId]

}

data class RedditFeedState(
    val stars: List<Star> = emptyList(),
)