package ponder.galaxy.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.galaxy.appDb
import ponder.galaxy.db.StarDao
import ponder.galaxy.db.toEntity
import ponder.galaxy.io.StarSocket
import ponder.galaxy.model.reddit.ListingType
import ponder.galaxy.model.reddit.RedditAuth
import pondui.LocalValueRepository
import pondui.ValueRepository
import ponder.galaxy.model.reddit.RedditClient
import pondui.ui.core.StateModel
import pondui.ui.core.ViewState

class RedditFeedModel(
    private val starSocket: StarSocket = StarSocket(),
    private val starDao: StarDao = appDb.getStarDao()
): StateModel<RedditFeedState>() {
    override val state = ViewState(RedditFeedState())

    // val messenger = MessengerModel()

    init {
        viewModelScope.launch {
            starSocket.starFlow.collect { stars ->
                starDao.upsert(*stars.map {it.toEntity() }.toTypedArray())
                setState { it -> it.copy(posts = stars.map { star -> RedditPost(star.title) })}
            }
        }

        viewModelScope.launch {
            starSocket.start()
        }
    }

}

data class RedditFeedState(
    val posts: List<RedditPost> = emptyList()
)

data class RedditPost(
    val title: String,
)