package ponder.galaxy.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.galaxy.globalProbeService
import ponder.galaxy.io.ProbeService
import ponder.galaxy.io.ProbeSocket
import ponder.galaxy.io.StarSource
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId
import pondui.ui.core.StateModel
import pondui.ui.core.ModelState

class RedditFeedModel(
    private val probeService: ProbeService = globalProbeService
): StateModel<RedditFeedState>() {
    override val state = ModelState(RedditFeedState())

    // val messenger = MessengerModel()


    init {
        viewModelScope.launch {
            probeService.stateFlow.collect { state ->
                val stars = state.stars.sortedByDescending { probeService.getRise(it.starId) }.take(100)

                setState { it -> it.copy(stars = stars) }
            }
        }
    }

    fun getStarLogs(starId: StarId) = probeService.getStarLogs(starId)

}

data class RedditFeedState(
    val stars: List<Star> = emptyList(),
)