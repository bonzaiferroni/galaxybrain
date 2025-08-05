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

    init {
        viewModelScope.launch {
            starSocket.starLogFlow.collect { starLogs ->
                val missingStarIds = starLogs.filter { starLog -> stateNow.stars.none { starLog.starId == it.starId } }
                    .map {it.starId}
                val missingStars = starSource.readStars(missingStarIds)
                val starLogMap = starSource.readStarLogs(missingStarIds).toMutableMap()
                for (starLog in starLogs) {
                    if (missingStarIds.any { starLog.starId == it }) continue
                    val currentList = stateNow.starLogMap[starLog.starId] ?: continue
                    starLogMap[starLog.starId] = currentList + starLog
                }
                val stars = (stateNow.stars.filter { star -> starLogs.any { star.starId == it.starId } } + missingStars)
                    .sortedByDescending { starLogMap[it.starId]?.lastOrNull()?.rise }
                setState { it -> it.copy(stars = stars, starLogMap = starLogMap) }
            }
        }

        viewModelScope.launch {
            starSocket.start()
        }
    }

}

data class RedditFeedState(
    val stars: List<Star> = emptyList(),
    val starLogMap: Map<StarId, List<StarLog>> = emptyMap()
)

data class RedditPost(
    val title: String,
)