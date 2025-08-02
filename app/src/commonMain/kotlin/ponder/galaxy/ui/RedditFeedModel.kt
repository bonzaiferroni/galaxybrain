package ponder.galaxy.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.galaxy.model.reddit.ListingType
import ponder.galaxy.model.reddit.RedditAuth
import pondui.LocalValueRepository
import pondui.ValueRepository
import ponder.galaxy.model.reddit.RedditClient
import pondui.ui.core.StateModel
import pondui.ui.core.ViewState

class RedditFeedModel(
    private val valueRepo: ValueRepository = LocalValueRepository(),
    private val client: RedditClient = RedditClient(
        auth = RedditAuth(
            username = valueRepo.readString(REDDIT_USERNAME_KEY),
            password = valueRepo.readString(REDDIT_PASSWORD_KEY),
            appId = valueRepo.readString(REDDIT_APP_ID_KEY),
            appSecret = valueRepo.readString(REDDIT_APP_SECRET_KEY)
        )
    )
): StateModel<RedditFeedState>() {
    override val state = ViewState(RedditFeedState())

    // val messenger = MessengerModel()

    init {
        viewModelScope.launch {
            val links = client.getListing("news", ListingType.Rising).map { RedditPost(it.title)}
            setState { it.copy(posts = links) }
        }
    }

}

data class RedditFeedState(
    val posts: List<RedditPost> = emptyList()
)

data class RedditPost(
    val title: String,
)

const val REDDIT_USERNAME_KEY = "REDDIT_USERNAME"
const val REDDIT_PASSWORD_KEY = "REDDIT_PASSWORD"
const val REDDIT_APP_ID_KEY = "REDDIT_APP_ID"
const val REDDIT_APP_SECRET_KEY = "REDDIT_APP_SECRET"